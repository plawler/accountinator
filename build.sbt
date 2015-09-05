name := """accountinator"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  ws,
  specs2 % Test,
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
  "com.stormpath.sdk" % "stormpath-sdk-api" % "1.0.RC4.5",
  "com.stormpath.sdk" % "stormpath-sdk-httpclient" % "1.0.RC4.5",
  "com.stormpath.sdk" % "stormpath-sdk-oauth" % "1.0.RC4.5",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.7.play24",
  "javax.servlet" % "javax.servlet-api" % "3.1.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.5",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "org.mockito" % "mockito-core" % "1.10.19",
//  "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.2" % "test"
  "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.3-SNAPSHOT"

)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator