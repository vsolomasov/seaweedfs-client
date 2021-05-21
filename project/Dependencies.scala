import sbt._

object Dependencies {

  object CompilerPlugin {
    val KindProjector = "org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full
    val BetterMonadic = "com.olegpy" %% "better-monadic-for" % "0.3.1"
  }

  object Cats {
    private val version = "2.6.1"
    val Core = "org.typelevel" %% "cats-core" % version
  }

  object Http4s {
    private val version = "0.21.22"
    val Dsl = "org.http4s" %% "http4s-dsl" % version
    val Client = "org.http4s" %% "http4s-blaze-client" % version
    val Circe = "org.http4s" %% "http4s-circe" % version
  }

  object Circe {
    private val version = "0.12.3"
    val Parser = "io.circe" %% "circe-parser" % version
    val Generic = "io.circe" %% "circe-generic" % version
  }

  object Log4Cats {
    private val version = "1.1.1"
    val Core = "io.chrisdavenport" %% "log4cats-core" % version
    val Slf4j = "io.chrisdavenport" %% "log4cats-slf4j" % version
  }

  val Logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
