import sbt.Keys._

//lazy val commonSettings = Seq(
//  version := "0.1",
//  organization := "nlp100",
//  scalaVersion := "2.11.7"
//)

name := "nlp100-root"

version := "0.1"

scalaVersion := "2.11.7"

lazy val nlp100 = project.in(file(".")).
  settings(
    name := "nlp100",
    version := "0.1",
    scalaVersion := "2.11.7",
    unmanagedClasspath in Runtime += baseDirectory.value / "resources",
    libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.4" withSources() withJavadoc(),
    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test" withSources() withJavadoc(),
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.3" withSources() withJavadoc(),
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3" withSources() withJavadoc(),
    libraryDependencies += "org.sameersingh.scalaplot" % "scalaplot" % "0.0.4" withSources() withJavadoc(),
    libraryDependencies += "com.github.rholder" % "snowball-stemmer" % "1.3.0.581.1" withSources() withJavadoc(),
    libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.5.0" withSources() withJavadoc() artifacts(Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")),
    libraryDependencies += "net.debasishg" %% "redisclient" % "2.13" withSources() withJavadoc(),
    libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.11",
    libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.11",
    libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.10",
    libraryDependencies += "de.bwaldvogel" % "liblinear" % "1.95",
    libraryDependencies += "org.mongodb" %% "casbah" % "3.0.0",
    scalacOptions += "-feature",
    scalacOptions in(Compile, doc) ++= Seq(
      "-diagrams",
      "-diagrams-dot-path", "/usr/local/bin/dot",
      "-doc-title", name.value
    )
  )
