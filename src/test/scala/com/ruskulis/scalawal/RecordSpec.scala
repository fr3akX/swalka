package com.ruskulis.scalawal

import java.io.ByteArrayInputStream

import org.scalatest.{FlatSpec, Matchers}

class RecordSpec extends FlatSpec with Matchers {
  it should "encode to bytes" in {
    val data = "some data"
    val record = Record.fromWriter(data.getBytes())
    val encoded = Record.toData(record)
    val decoded = Record.fromBytes(new ByteArrayInputStream(encoded))
    new String(decoded._2.data) shouldBe data
  }
}
