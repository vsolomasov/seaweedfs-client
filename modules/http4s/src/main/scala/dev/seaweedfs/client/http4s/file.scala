package dev.seaweedfs.client.http4s

import cats.effect.{Blocker, ContextShift, Sync}
import cats.syntax.all._
import fs2.{Stream, io}
import org.http4s.multipart.Part

import java.io.File
import java.nio.file.Files

object file {

  trait FileUtil[F[_]] {
    def createPart(file: File): F[Part[F]]
    def save(stream: Stream[F, Byte]): F[File]
  }

  class FileUtilInterpreter[F[_]: Sync: ContextShift](blocker: Blocker) extends FileUtil[F] {
    override def createPart(file: File): F[Part[F]] = Sync[F].delay {
      Part.fileData("file", file, blocker)
    }

    override def save(stream: Stream[F, Byte]): F[File] = {
      for {
        path <- Sync[F].delay(Files.createTempFile("", ""))
        _ <- stream.through(io.file.writeAll(path, blocker)).compile.drain
      } yield path.toFile
    }
  }

  object FileUtilInterpreter {
    def make[F[_]: Sync: ContextShift](blocker: Blocker): F[FileUtil[F]] =
      Sync[F].delay(new FileUtilInterpreter[F](blocker))
  }
}
