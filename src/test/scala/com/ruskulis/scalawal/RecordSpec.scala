package com.ruskulis.scalawal
import org.scalatest.{FlatSpec, Matchers}

class RecordSpec extends FlatSpec with Matchers {
  it should "encode to bytes" in {
    val data = "some data"
    val record = Record.fromWriter(data.getBytes())
    val encoded = Record.toData(record)
    succeed
  }
}
