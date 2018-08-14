package com.ruskulis.scalawal

import java.io.InputStream
import java.util.zip.CRC32
import java.nio.ByteBuffer


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
  def fromWriter(data: Array[Byte]): Record = Record(data.length, checksum(data), data)

  def toData(record: Record): Array[Byte] =
    intToBytes(record.length) ++ longToBytes(record.checksum) ++ record.data

  def fromBytes(in: InputStream): (Long, Record) = {
    val buffer = new Array[Byte](intSize)
    in.read(buffer)
    val size = bytesToInt(buffer)

    val cbuf = new Array[Byte](longSize)
    in.read(cbuf)
    val checksm = bytesToLong(cbuf)

    val vbuf = new Array[Byte](size)
    in.read(vbuf)

    if(checksum(vbuf) != checksm) sys.error("crc32 mismatch, corrupted data")

    (intSize + longSize + size, Record(size, checksm, vbuf))
  }

  def checksum(data: Array[Byte]): Long = {
    val crc32 = new CRC32()
    crc32.update(data)
    crc32.getValue
  }

  val longSize = java.lang.Long.BYTES
  val intSize = java.lang.Integer.BYTES

  def readLong(in: InputStream): Long = {
    val cbuf = new Array[Byte](longSize)
    in.read(cbuf)
    bytesToLong(cbuf)
  }

  def intToBytes(x: Int): Array[Byte] = {
    val buffer = ByteBuffer.allocate(intSize)
    buffer.putInt(x)
    buffer.array
  }

  def bytesToInt(bytes: Array[Byte]): Int = {
    val buffer = ByteBuffer.allocate(intSize)
    buffer.put(bytes)
    buffer.flip //need flip

    buffer.getInt()
  }

  def longToBytes(x: Long): Array[Byte] = {
    val buffer = ByteBuffer.allocate(longSize)
    buffer.putLong(x)
    buffer.array
  }

  def bytesToLong(bytes: Array[Byte]): Long = {
    val buffer = ByteBuffer.allocate(longSize)
    buffer.put(bytes)
    buffer.flip //need flip

    buffer.getLong
  }
}