package dev.seaweedfs.client.http4s

import cats.effect.{Blocker, ContextShift, Sync}
import org.http4s.multipart.Part

import java.io.File

object part {

  trait PartUtil[F[_]] {
    def create(file: File, blocker: Blocker): F[Part[F]]
  }

  class PartUtilInterpreter[F[_]: Sync: ContextShift] extends PartUtil[F] {
    override def create(file: File, blocker: Blocker): F[Part[F]] = Sync[F].delay {
      Part.fileData("file", file, blocker)
    }
  }

  object PartUtilInterpreter {
    def make[F[_]: Sync: ContextShift]: F[PartUtil[F]] = Sync[F].delay(new PartUtilInterpreter[F])
  }
}
