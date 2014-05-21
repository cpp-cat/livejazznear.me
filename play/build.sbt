name := """livejazznear.me"""

version := "0.1-SNAPSHOT"

play.Project.playScalaSettings

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "joda-time" % "joda-time" % "2.3",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.8",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "org.scalatestplus" %% "play" % "1.0.0" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)

