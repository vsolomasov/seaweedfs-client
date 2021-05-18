import Dependencies._

ThisBuild / version := "dev"
ThisBuild / scalaVersion := "2.13.5"

lazy val `seaweedfs-root` = project
  .in(file("."))
  .aggregate(
    `seaweedfs-client`,
    `seaweedfs-example`
  )

lazy val `seaweedfs-client` = project
  .in(file("modules/client"))
  .settings(
    libraryDependencies ++= Seq(
      Cats.Core,
      Http4s.Dsl,
      Http4s.Client,
      Http4s.Circe,
      Circe.Parser,
      Circe.Generic,
      Logging.log4cats
    ),
    addCompilerPlugin(CompilerPlugin.KindProjector),
    addCompilerPlugin(CompilerPlugin.BetterMonadic)
  )

lazy val `seaweedfs-example` = project
  .in(file("modules/example"))
  .dependsOn(`seaweedfs-client`)
  .settings(libraryDependencies += Logging.logback % Runtime)
