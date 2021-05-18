package dev.seaweedfs.client.example

import cats.effect.{ExitCode, IO, IOApp, Resource, Sync}
import cats.syntax.all._
import dev.seaweedfs.client.Protocol.WriteInfo
import dev.seaweedfs.client.domain.{ContentType, Photo, SeaweedFSConfig}
import dev.seaweedfs.client.{Commands, SeaweedFS}

import java.nio.file.{Files, Paths}

object Example extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val resource = SeaweedFS.make[IO](SeaweedFSConfig("127.0.0.1", 9333))(executionContext)
    savePng[IO](resource).map(_ => ExitCode.Success)
  }

  private def savePng[F[_]: Sync](commands: Resource[F, Commands[F]]): F[WriteInfo] = {
    for {
      png <- loadImage("modules/example/src/main/resources/inst.png", ContentType.Png)
      res <- commands.use(_.save(png))
    } yield res
  }

  private def loadImage[F[_]: Sync](path: String, contentType: ContentType): F[Photo] = {
    for {
      path <- Sync[F].delay(Paths.get(path))
      file <- Sync[F].delay(Files.readAllBytes(path))
    } yield Photo("inst.png", contentType, file)
  }
}
