name := """livejazznear.me"""

version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "joda-time" % "joda-time" % "2.3",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.8",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "org.scalatest" %% "scalatest" % "2.1.7" % "test",
  "org.scalatestplus" %% "play" % "1.1.0" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test"
)

scalacOptions += "-feature"

