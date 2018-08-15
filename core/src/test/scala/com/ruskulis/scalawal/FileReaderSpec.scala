package com.ruskulis.scalawal

import com.ruskulis.scalawal.offset.FileOffset
import com.ruskulis.scalawal.reader.FileReader
import org.scalatest.{FlatSpec, Matchers}

class FileReaderSpec extends FlatSpec with Matchers {
  it should "be able to read written log from beginning" in {

    val offset = new FileOffset

    println(s"OFFSET: ${offset.current}")
    def incrementallyRead(): Unit = {
      val reader = new FileReader(offset.current)

      if(reader.hasNext) {
        val c = reader.next
        println(s"from ofset: ${offset.current}, " + c.offset + " " + new String(c.data))
        reader.close
        offset.commit(c.offset)

        incrementallyRead()
      } else {
        reader.close
      }
    }

    incrementallyRead()
    succeed
  }
}