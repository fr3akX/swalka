package swalka.reader

import swalka.offset.Offset.Current
import swalka.reader.Reader.Result

import scala.language.higherKinds

trait Reader {
  type R <: Result

  def next: R

  def hasNext: Boolean

  def close: Unit
}

object Reader {
  trait Result {
    def offset: Current
    def data: Array[Byte]
  }

  case class ReadResult(offset: Current, data: Array[Byte]) extends Result
}