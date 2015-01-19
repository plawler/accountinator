name := """chorely-accounts"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
  "com.stormpath.sdk" % "stormpath-sdk-api" % "1.0.RC2.1",
  "com.stormpath.sdk" % "stormpath-sdk-httpclient" % "1.0.RC2.1",
  "com.stormpath.sdk" % "stormpath-sdk-oauth" % "1.0.RC2.1",
  "javax.servlet" % "javax.servlet-api" % "3.1.0",
  "org.mockito" % "mockito-all" % "1.9.5"
)
