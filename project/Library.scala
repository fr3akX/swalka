import sbt._

object Library {
  val akka = Seq(
    "com.typesafe.akka" %% "akka-stream",
    "com.typesafe.akka" %% "akka-actor"
  ).map(_ % "2.5.14")
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
}
