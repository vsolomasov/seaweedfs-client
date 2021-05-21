import Dependencies._

ThisBuild / version := "dev"
ThisBuild / scalaVersion := "2.13.5"

val commonSettings = Seq(
  organizationName := "SeaweedFS client for Scala",
  startYear := Some(2021),
  addCompilerPlugin(CompilerPlugin.KindProjector),
  addCompilerPlugin(CompilerPlugin.BetterMonadic)
)

lazy val `seaweedfs-root` = project
  .in(file("."))
  .aggregate(
    `seaweedfs-client`,
    `seaweedfs-http4s`,
    `seaweedfs-example`
  )

lazy val `seaweedfs-client` = project
  .in(file("modules/client"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(Cats.Core, Circe.Parser, Circe.Generic, Log4Cats.Core))

lazy val `seaweedfs-http4s` = project
  .in(file("modules/http4s"))
  .dependsOn(`seaweedfs-client`)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(Http4s.Dsl, Http4s.Client, Http4s.Circe))

lazy val `seaweedfs-example` = project
  .in(file("modules/example"))
  .dependsOn(`seaweedfs-http4s`)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(Log4Cats.Slf4j, Logback))
