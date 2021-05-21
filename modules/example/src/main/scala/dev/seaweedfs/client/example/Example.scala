package dev.seaweedfs.client.example

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Sync}
import cats.syntax.all._
import dev.seaweedfs.client.Commands
import dev.seaweedfs.client.Commands.PhotoInfo
import dev.seaweedfs.client.domain.{ContentType, Photo, SeaweedFSConfig}
import dev.seaweedfs.client.http4s.SeaweedFS
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import java.nio.file.{Files, Paths}
import scala.concurrent.ExecutionContext

object Example extends IOApp {
  val config: SeaweedFSConfig = SeaweedFSConfig("127.0.0.1", 9333)

  class Image(val path: String, val name: String, val contentType: ContentType)
  val png = new Image("modules/example/src/main/resources/inst.png", "inst.png", ContentType.Png)

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val context: ExecutionContext = executionContext
    execute[IO](config).map(_ => ExitCode.Success)
  }

  def execute[F[_]: ConcurrentEffect](config: SeaweedFSConfig)(implicit context: ExecutionContext): F[Unit] = {
    Slf4jLogger.create[F].flatMap { implicit logger =>
      SeaweedFS.make[F](config).use { implicit commands =>
        (saveImage andThen (getUrl *> removeImage)).apply(png)
      }
    }
  }

  private def saveImage[F[_]: Sync: Logger](implicit commands: Commands[F]) = {
    Kleisli.apply[F, Image, PhotoInfo] { image =>
      loadImage(image).flatMap(commands.save) <* Logger[F].info(s"Image '${image.name}' has been successfully saved")
    }
  }

  private def getUrl[F[_]: Sync: Logger](implicit commands: Commands[F]) = {
    Kleisli.apply[F, PhotoInfo, Unit] { photoInfo =>
      commands.search(photoInfo.fid).flatMap {
        case Some(url) => Logger[F].info(s"Image path: $url")
        case None => Logger[F].warn(s"Image with fid ${photoInfo.fid} not found")
      }
    }
  }

  private def removeImage[F[_]: Sync: Logger](implicit commands: Commands[F]) = {
    Kleisli.apply[F, PhotoInfo, Unit](photoInfo => commands.remove(photoInfo.fid) *> Logger[F].info("Image deleted"))
  }

  private def loadImage[F[_]: Sync](image: Image): F[Photo] = {
    for {
      path <- Sync[F].delay(Paths.get(image.path))
      file <- Sync[F].delay(Files.readAllBytes(path))
    } yield Photo(image.name, image.contentType, file)
  }
}
