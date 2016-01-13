name := "id-macro"

version := "0.1"

organization := "com.evojam"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)

lazy val root =
  project.in(file("."))
    .settings(
      scalaVersion := "2.11.7",
      run <<= run in Compile in core,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
.aggregate(macros, core)

lazy val macros =
  project
    .in(file("macros"))
    .settings(
      scalaVersion := "2.11.7",
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      libraryDependencies ++= Seq(
        "org.specs2" %% "specs2-core" % "3.7" % "test",
        "com.kifi" %% "json-annotation" % "0.2"),
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _))

lazy val core =
  project
    .in(file("core"))
    .settings(scalaVersion := "2.11.7",
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
    .dependsOn(macros)