name := """zenoel"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.scalacheck" %% "scalacheck" % "1.13.0" % "test",
  "org.specs2" %% "specs2-core" % "3.8.3" % "test",
  "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3"

)

routesGenerator := InjectedRoutesGenerator

routesImport ++= Seq("events._", "support.Binders._")

TwirlKeys.templateImports ++= Seq("events._")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"