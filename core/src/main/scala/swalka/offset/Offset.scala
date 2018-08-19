package swalka.offset

import scala.language.higherKinds

trait Offset {
  def commit(pos: Long): Unit
  def current: Long
}
