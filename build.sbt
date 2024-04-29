import Dependencies._

lazy val root= (project in file("."))
  .settings(
    scalaVersion := "2.13.6",
    name := "smartcloud-prices",
    scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings"))),
    libraryDependencies ++= Seq(
      Libraries.http4s("ember-server"),
      Libraries.http4s("ember-client"),
      Libraries.http4s("circe"),
      Libraries.http4s("dsl"),
      Libraries.circe,
      Libraries.log4cats,
      Libraries.logback,
      Libraries.pureConfig,
      TestLibraries.munit,
      CompilerPlugins.betterMonadicFor,
      CompilerPlugins.kindProjector
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
