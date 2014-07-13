name := """livejazznear.me"""

version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.typesafe.slick" %% "slick" % "2.0.2",
//  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "joda-time" % "joda-time" % "2.3",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.8",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "org.apache.spark"  %% "spark-core"    % "1.1.0-SNAPSHOT",
  "org.eclipse.jetty.orbit" % "javax.transaction" % "1.1.1.v201105210645",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016",
  "org.eclipse.jetty.orbit" % "javax.mail.glassfish" % "1.4.1.v201005082020",
  "com.google.protobuf" % "protobuf-java" % "2.5.0",
  "org.scalatest" %% "scalatest" % "2.1.7" % "test",
  "org.scalatestplus" %% "play" % "1.1.0" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test"
)

resolvers += "Local Maven Repository" at "file:///home/michel/.m2/repository/"

scalacOptions += "-feature"

