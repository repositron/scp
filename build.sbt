import Dependencies.*
import org.typelevel.scalacoptions.ScalacOptions

lazy val root= (project in file("."))
  .settings(
    scalaVersion := "3.4.1",
    name := "smartcloud-prices",
    tpolecatExcludeOptions := Set(
      ScalacOptions.fatalWarnings
    ),
    libraryDependencies ++= Seq(
      Libraries.http4s("ember-server"),
      Libraries.http4s("ember-client"),
      Libraries.http4s("circe"),
      Libraries.http4s("dsl"),
      Libraries.circe,
      Libraries.circeLit,
      Libraries.log4cats,
      Libraries.logback,
      Libraries.pureConfig,
      TestLibraries.munit,
      TestLibraries.munitCatsEffect
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
