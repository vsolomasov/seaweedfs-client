package dev.seaweedfs.client.example

import cats.data.Kleisli
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Sync}
import cats.syntax.all._
import dev.seaweedfs.client.Commands
import dev.seaweedfs.client.Commands.PhotoInfo
import dev.seaweedfs.client.domain.SeaweedFSConfig
import dev.seaweedfs.client.http4s.SeaweedFS
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import java.io.File
import scala.concurrent.ExecutionContext

object Example extends IOApp {
  val config: SeaweedFSConfig = SeaweedFSConfig("127.0.0.1", 9333)

  override def run(args: List[String]): IO[ExitCode] = Blocker[IO].use {
    execute[IO](config)(executionContext, _).map(_ => ExitCode.Success)
  }

  def execute[F[_]: ConcurrentEffect: ContextShift](
    config: SeaweedFSConfig
  )(context: ExecutionContext, blocker: Blocker): F[Unit] = {
    Slf4jLogger.create[F].flatMap { implicit logger =>
      SeaweedFS.make[F](config)(context, blocker).use { implicit commands =>
        (saveFile andThen (getUrl *> removeFile))
          .apply("modules/example/src/main/resources/inst.png")
      }
    }
  }

  private def saveFile[F[_]: Sync: Logger](implicit commands: Commands[F]) = {
    Kleisli.apply[F, String, PhotoInfo] { path =>
      for {
        file <- Sync[F].delay(new File(path))
        photoInfo <- commands.save(file)
        _ <- Logger[F].info(s"File has been successfully saved")
      } yield photoInfo
    }
  }

  private def getUrl[F[_]: Sync: Logger](implicit commands: Commands[F]) = {
    Kleisli.apply[F, PhotoInfo, Unit] { photoInfo =>
      commands.search(photoInfo.fid).flatMap {
        case Some(url) => Logger[F].info(s"File path: $url")
        case None => Logger[F].warn(s"File with fid ${photoInfo.fid} not found")
      }
    }
  }

  private def removeFile[F[_]: Sync: Logger](implicit commands: Commands[F]) = {
    Kleisli.apply[F, PhotoInfo, Unit](photoInfo => commands.remove(photoInfo.fid) *> Logger[F].info("File was deleted"))
  }

//  private def loadImage[F[_]: Sync](image: Image): F[Photo] = {
//    for {
//      path <- Sync[F].delay(Paths.get(image.path))
//      file <- Sync[F].delay(Files.readAllBytes(path))
//    } yield Photo(image.name, image.contentType, file)
//  }
}
