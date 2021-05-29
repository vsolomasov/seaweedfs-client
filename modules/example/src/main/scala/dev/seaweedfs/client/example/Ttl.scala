package dev.seaweedfs.client.example

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import cats.syntax.all._
import dev.seaweedfs.client.Commands
import dev.seaweedfs.client.Commands.FileInfo
import io.chrisdavenport.log4cats.Logger

import java.io.File
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

trait Ttl {

  def ttl[F[_]: ConcurrentEffect: ContextShift: Logger](commands: Commands[F], path: String, ttl: FiniteDuration): F[Unit] = {
    implicit val _commands: Commands[F] = commands
    (saveFileWithTtl(ttl) andThen (showUrl *> changeTtl(ttl.plus(FiniteDuration(1, TimeUnit.MINUTES))) andThen showUrl)).apply(path)
  }

  private def saveFileWithTtl[F[_]: Sync: Logger](ttl: FiniteDuration)(implicit commands: Commands[F]) = {
    Kleisli.apply[F, String, FileInfo] { path =>
      for {
        file <- Sync[F].delay(new File(path))
        photoInfo <- commands.save(file, ttl.some)
        _ <- Logger[F].info(s"File has been successfully saved with TTL: $ttl min")
      } yield photoInfo
    }
  }

  private def changeTtl[F[_]: Sync: Logger](ttl: FiniteDuration)(implicit commands: Commands[F]) = {
    Kleisli.apply[F, FileInfo, FileInfo] { fileInfo =>
      commands.changeTtl(fileInfo.fid, ttl.some) <*
        Logger[F].info(s"Ttl has been successfully changed to: ${ttl.toMinutes} min")
    }
  }

  private def showUrl[F[_]: Sync: Logger](implicit commands: Commands[F]) = {
    Kleisli.apply[F, FileInfo, Unit] { photoInfo =>
      commands.search(photoInfo.fid).flatMap {
        case Some(url) => Logger[F].info(s"File path: $url")
        case None => Logger[F].warn(s"File with fid ${photoInfo.fid} not found")
      }
    }
  }
}
