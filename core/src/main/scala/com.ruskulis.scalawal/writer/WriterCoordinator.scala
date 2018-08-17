package com.ruskulis.scalawal.writer

import java.io.FileOutputStream
import java.nio.ByteBuffer

import cats.Id

import scala.reflect.io.Path

class WriterCoordinator(path: Path) extends Writer[Id] {

  private val fos = new FileOutputStream(path./("segments").toAbsolute.toString(), true)
  private val c = fos.getChannel

  def currentSegment: String = ???


  override def write(data: ByteBuffer): Id[Unit] = ???
  override def flush: Id[Unit] = ???
  override def close: Id[Unit] = ???
}