package swalka

import java.io.{ByteArrayOutputStream, InputStream}
import java.nio.ByteBuffer
import java.nio.channels.{FileChannel, SeekableByteChannel}
import java.util.zip.{CRC32, GZIPOutputStream}


/**
  * Seq can be file offset
  * in case of multiple file, pointer to file can be added
  *
  * data alignment:
  * size | checksum | data
  *
  * @param seq
  * @param length
  * @param checksum
  * @param data
  */
case class Record(length: Int, checksum: Long, data: Array[Byte])

object Record {

  def wrap(data: Array[Byte]): Record = Record(data.length, checksum(data), data)

  def toByteBuffer(record: Record): ByteBuffer = {
    val bb = ByteBuffer.allocate(longSize + intSize + record.length)
    bb
      .putInt(record.length)
      .putLong(record.checksum)
      .put(record.data)
  }

  def fromChannel(channel: SeekableByteChannel): (Long, Record) = {
    val sizeBB = ByteBuffer.allocate(intSize)
    channel.read(sizeBB)
    sizeBB.flip()
    val size = sizeBB.getInt()
    val dataBB = ByteBuffer.allocate(longSize + size)
    channel.read(dataBB)
    dataBB.flip()
    val crc32 = dataBB.getLong()
    val dataArr = new Array[Byte](size)
    dataBB.get(dataArr, 0, size)
    if (checksum(dataArr) != crc32) sys.error("crc32 mismatch, corrupted data")

    (intSize + longSize + size, Record(size, crc32, dataArr))
  }

  def checksum(data: Array[Byte]): Long = {
    val crc32 = new CRC32()
    crc32.update(data)
    crc32.getValue
  }

  def compressed(record: Record): Record = {
    val compressed = gzip(record.data)
    Record(compressed.length, checksum(compressed), compressed)
  }

  def gzip(data: Array[Byte]): Array[Byte] = {
    val byteStream = new ByteArrayOutputStream(data.length)
    val zipStream = new GZIPOutputStream(byteStream)
    zipStream.write(data)
    zipStream.close()
    byteStream.close()
    byteStream.toByteArray
  }

  def make(data: Array[Byte]): ByteBuffer = toByteBuffer(wrap(data))
  def make(data: String): ByteBuffer = make(data.getBytes())
}