package com.ruskulis.scalawal.reader

import com.ruskulis.scalawal.reader.Reader.ReadResult

import scala.language.higherKinds

trait Reader {
  def next: ReadResult
  def hasNext: Boolean
  def close: Unit
}

object Reader {
  case class ReadResult(offset: Long, data: Array[Byte])
}