
val defaultSettings = Seq(
  version := "0.1.5",
  scalaVersion := "2.12.6",
  organization := "swalka"
)

lazy val core = project
  .in(file("core"))
  .settings(
    moduleName := "core",
    libraryDependencies ++= Library.logging :+ Library.scalaTest
  )
  .settings(defaultSettings)

lazy val test = project
  .in(file("test"))
  .settings(moduleName := "test")
  .settings(defaultSettings)
  .dependsOn(core, `akka-streams`)

lazy val benchmark = project
  .in(file("benchmark"))
  .settings(moduleName := "benchmark")
  .settings(defaultSettings)
  .enablePlugins(JmhPlugin)
  .dependsOn(core)

lazy val `akka-streams` = project
  .in(file("akka-streams"))
  .settings(moduleName := "akka-streams")
  .settings(defaultSettings)
  .settings(
    libraryDependencies ++= Library.akka
  )
  .dependsOn(core)

lazy val root = Project("swalka", file("."))
  .settings(moduleName := "swalka")
  .settings(defaultSettings)
  .settings(
    git.remoteRepo := "git@github.com:fr3akX/swalka.git",
    sourceDirectory in Paradox := sourceDirectory.value / "site",
    paradoxTheme := Some(builtinParadoxTheme("generic"))
  )
  .aggregate(core, test, benchmark, `akka-streams`)
  .enablePlugins(GhpagesPlugin, ParadoxSitePlugin)

addCommandAlias("bench", "; project benchmark; jmh:run -i 1 -wi 3 -f1 -t1")
addCommandAlias("site", "; makeSite; ghpagesPushSite")