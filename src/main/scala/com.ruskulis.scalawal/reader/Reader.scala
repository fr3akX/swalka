package com.ruskulis.scalawal.reader

import com.ruskulis.scalawal.reader.Reader.ReadResult

import scala.language.higherKinds

trait Reader[F[_]] {
  def next: F[ReadResult]
  def hasNext: F[Boolean]
  def close: F[Unit]
}

object Reader {
  case class ReadResult(offset: Long, data: Array[Byte])
}