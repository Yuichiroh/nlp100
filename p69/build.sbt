name := """p69"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.11.2.play24"

libraryDependencies += "org.webjars" %% "webjars-play" % "2.4.0-1"

libraryDependencies += "org.webjars" % "jquery" % "2.1.4"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
