package com.ruskulis.scalawal.writer


import java.nio.ByteBuffer

import scala.language.higherKinds

trait Writer[F[_]] {
  def write(data: ByteBuffer): F[Unit]
  def flush: F[Unit] //intended for fsync
  def close: F[Unit]
}
