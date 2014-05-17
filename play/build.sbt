name := """livejazznear.me"""

version := "0.1-SNAPSHOT"

play.Project.playScalaSettings

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "joda-time" % "joda-time" % "2.3",
  "org.scalatestplus" %% "play" % "1.0.0" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)

