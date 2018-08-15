
val defaultSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.6"
)

lazy val core = project
  .in(file("core"))
  .settings(
    moduleName := "core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "1.0.0-RC2",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .settings(defaultSettings)

lazy val test = project
  .in(file("test"))
  .settings(moduleName := "test")
  .settings(defaultSettings)
  .dependsOn(core)

lazy val benchmark = project
  .in(file("benchmark"))
  .settings(moduleName := "benchmark")
  .settings(defaultSettings)
  .enablePlugins(JmhPlugin)
  .dependsOn(core)

lazy val root = Project("scalawal", file("."))
  .settings(moduleName := "scalawal")
  .settings(defaultSettings)
  .aggregate(core, test, benchmark)

addCommandAlias("bench", "; project benchmark; jmh:run -i 1 -wi 3 -f1 -t1")