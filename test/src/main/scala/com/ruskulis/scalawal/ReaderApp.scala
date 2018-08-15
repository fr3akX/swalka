package com.ruskulis.scalawal

import com.ruskulis.scalawal.offset.FileOffset
import com.ruskulis.scalawal.reader.FileReader

object ReaderApp extends App {
  def doRead(): Unit = {
    val offset = new FileOffset
    val reader = new FileReader(offset.current)
    if(reader.hasNext) {
      reader.next
      println("")
    } else {
      println("eof reached, ppolling")
      reader.close
      offset.close
      Thread.sleep(1000)
      doRead()
    }
  }


  doRead()
}
