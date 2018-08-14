package com.ruskulis.scalawal.offset

import scala.language.higherKinds

trait Offset[F[_]] {
  def commit(pos: Long): F[Unit]
  def current: F[Long]
}
