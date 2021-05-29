package dev.seaweedfs.client.example

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import cats.syntax.all._
import dev.seaweedfs.client.Commands
import dev.seaweedfs.client.Commands.PhotoInfo
import io.chrisdavenport.log4cats.Logger

import java.io.File

trait Ttl {

  def ttl[F[_]: ConcurrentEffect: ContextShift: Logger](commands: Commands[F], path: String, ttl: Long): F[Unit] = {
    implicit val _commands: Commands[F] = commands
    (saveFileWithTtl(ttl) andThen getUrl).apply(path)
  }

  private def saveFileWithTtl[F[_]: Sync: Logger](ttl: Long)(implicit commands: Commands[F]) = {
    Kleisli.apply[F, String, PhotoInfo] { path =>
      for {
        file <- Sync[F].delay(new File(path))
        photoInfo <- commands.save(file, ttl.some)
        _ <- Logger[F].info(s"File has been successfully saved with TTL: $ttl min")
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
}
