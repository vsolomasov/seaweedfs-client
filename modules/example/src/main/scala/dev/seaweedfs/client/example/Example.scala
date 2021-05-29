package dev.seaweedfs.client.example

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp}
import cats.syntax.all._
import dev.seaweedfs.client.domain.SeaweedFSConfig
import dev.seaweedfs.client.http4s.SeaweedFS
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

object Example extends IOApp with LifeCycle with Ttl {
  val config: SeaweedFSConfig = SeaweedFSConfig("127.0.0.1", 9333)

  override def run(args: List[String]): IO[ExitCode] = Blocker[IO].use {
    execute[IO](config)(executionContext, _).map(_ => ExitCode.Success)
  }

  def execute[F[_]: ConcurrentEffect: ContextShift](
    config: SeaweedFSConfig
  )(context: ExecutionContext, blocker: Blocker): F[Unit] = {
    Slf4jLogger.create[F].flatMap { implicit logger =>
      SeaweedFS.make[F](config)(context, blocker).use { implicit commands =>
        lifeCycle[F](commands, "modules/example/src/main/resources/inst.png") *>
          ttl[F](commands, "modules/example/src/main/resources/inst.png", 1)
      }
    }
  }
}
