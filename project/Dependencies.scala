import sbt._

object Dependencies {

  object Versions {
    // Scala

    val http4s     = "1.0.0-M41"
    val circe      = "0.14.7"
    val pureConfig = "0.17.6"
    val log4cats   = "2.6.0"
    val logBackVersion = "1.5.6"

    // Test
    val munit = "0.7.29"

    // Compiler
    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.3"
  }

  object Libraries {
    // Scala
    def http4s(module: String): ModuleID = "org.http4s" %% s"http4s-$module" % Versions.http4s

    val circe      = "io.circe"              %% "circe-generic"   % Versions.circe
    val log4cats   = "org.typelevel"         %% "log4cats-slf4j"  % Versions.log4cats
    val logback    = "ch.qos.logback"        % "logback-classic"  % Versions.logBackVersion
    val pureConfig = "com.github.pureconfig" %% "pureconfig"      % Versions.pureConfig

  }

  object TestLibraries {
    // Scala
    val munit = "org.scalameta" %% "munit" % Versions.munit % Test
  }

  object CompilerPlugins { // Compiler plugins
    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor)
    val kindProjector    = compilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector cross CrossVersion.full)
  }

}
