package swalka.util


class Resource[Res, Result](private val init: () => Res, private val close: Res => Unit, private val thunk: Res => Result) {

  def map[B](f: Result => B): Resource[Res, B] =
    new Resource(init, close, thunk = thunk andThen f)

  def flatMap[B, Res2](f: Result => Resource[Res2, B]): Resource[Res2, B] = {

    //todo refactor to free
    lazy val res = {
      val r = init()
      val result = thunk(r)
      (r, f(result))
    }

    new Resource(
      () => {
        val (_, r2) = res
        r2.init()
      },
      res2 => {
        val (r, r2) = res
        r2.close(res2)
        close(r)
      },
      r => {
        val (_, r2) = res
        r2.thunk(r)
      })
  }

  def run(): Result = {
    val r = init()
    try {
      thunk(r)
    } finally {
      close(r)
    }
  }
}



object Resource {
  def apply[Res, Result](acquire: () => Res, release: Res => Unit)(thunk: Res => Result) =
    new Resource[Res, Result](acquire, release, thunk)
}
