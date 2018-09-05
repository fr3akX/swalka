package swalka.util

import org.scalatest.{FlatSpec, Matchers}

class ResourceSpec extends FlatSpec with Matchers {
  it should "manage resources" in {
    def mkres(id: String): AutoCloseable = () => { println(s"aim closing $id") }

    val r1 = Resource[AutoCloseable, Unit](() => mkres("1"), r => r.close())(_ => println("acquired 1"))
    val r2 = Resource[AutoCloseable, Unit](() => mkres("2"), r => r.close())(_ => println("acquired 2"))
    val r3 = Resource[AutoCloseable, Unit](() => mkres("3"), r => r.close())(_ => println("acquired 3"))

    val x = for {
      a <- r1
      b <- r2
      c <- r3
    } yield (a, b, c)

    println("XXX executing")
    val xxx = x.run()

    succeed
  }
}
