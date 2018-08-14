package com.ruskulis.scalawal.writer


import scala.language.higherKinds

trait Writer[F[_]] {
  def write(data: Array[Byte]): F[Unit]
  def flush: F[Unit] //intended for fsync
  def close: F[Unit]
}
