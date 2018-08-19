package swalka.offset

import swalka.offset.Offset.Current

import scala.language.higherKinds

trait Offset {
  def commit(offset: Current): Unit
  def current: Current
}

object Offset {
  case class Current(segment: Int, pos: Long)
}