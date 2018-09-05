package swalka

object Journal {
  val JournalFilePrefix = "journal."
  def segment(seg: Int): String = s"$JournalFilePrefix$seg"
}