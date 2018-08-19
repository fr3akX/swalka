package swalka

import org.scalatest.{FlatSpec, Matchers}
import swalka.offset.FileOffset
import swalka.offset.Offset.Current
import swalka.reader.FileReader

class FileReaderSpec extends FlatSpec with Matchers {

  it should "be able to read written log from beginning" in Util.withWrittenLog { path =>

      val offset = new FileOffset(path)

      println(s"OFFSET: ${offset.current}")
      def incrementallyRead(): Unit = {
        val reader = new FileReader(path, 0, offset.current.pos)

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
