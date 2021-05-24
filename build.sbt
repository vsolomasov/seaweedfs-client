import Dependencies._

ThisBuild / version := "dev"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / organization := "com.github.vsolomasov"

val commonSettings = Seq(
  organizationName := "SeaweedFS client for Scala",
  startYear := Some(2021),
  Compile / packageDoc / publishArtifact := false,
  addCompilerPlugin(CompilerPlugin.KindProjector),
  addCompilerPlugin(CompilerPlugin.BetterMonadic)
)

lazy val `seaweedfs-root` = project
  .in(file("."))
  .aggregate(
    `seaweedfs-core`,
    `seaweedfs-http4s`,
    `seaweedfs-example`
  )

lazy val `seaweedfs-core` = project
  .in(file("modules/core"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(Cats.Core, Circe.Parser, Circe.Generic, Log4Cats.Core))

lazy val `seaweedfs-http4s` = project
  .in(file("modules/http4s"))
  .dependsOn(`seaweedfs-core`)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(Http4s.Dsl, Http4s.Client, Http4s.Circe, Log4Cats.Slf4j))

lazy val `seaweedfs-example` = project
  .in(file("modules/example"))
  .dependsOn(`seaweedfs-http4s`)
  .settings(commonSettings)
  .settings(libraryDependencies += Logback)
