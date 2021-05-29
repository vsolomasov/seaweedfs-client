package dev.seaweedfs.client.interpreters

import cats.MonadThrow
import cats.syntax.all._
import dev.seaweedfs.client.Commands.FileInfo
import dev.seaweedfs.client.interpreters.CommandsInterpreter.decomposeFid
import dev.seaweedfs.client.{Commands, Protocol}

import java.io.File
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

class CommandsInterpreter[F[_]: MonadThrow](
  protocol: Protocol[F]
) extends Commands[F] {
  private val ME: MonadThrow[F] = implicitly

  override def save(file: File, ttl: Option[FiniteDuration]): F[FileInfo] = {
    for {
      assignInfo <- protocol.getAssign(ttl)
      writeInfo <- protocol.save(assignInfo, file, ttl)
    } yield FileInfo(assignInfo, writeInfo)
  }

  override def changeTtl(fid: String, ttl: Option[FiniteDuration]): F[FileInfo] = {
    for {
      (volumeId, _) <- decomposeFid(fid)
      locationInfo <- protocol.location(volumeId)
      location <- ME.fromOption(locationInfo.locations.headOption, new Exception("File not found!"))
      file <- protocol.extract(fid, location)
      fileInfo <- save(file, ttl)
//      _ <- locationInfo.locations.traverse(protocol.remove(fid, _))
    } yield fileInfo
  }

  override def search(fid: String): F[Option[String]] = {
    for {
      (volumeId, _) <- decomposeFid(fid)
      locationInfo <- protocol.location(volumeId)
    } yield locationInfo.locations.headOption.map(location => s"${location.publicUrl}/$fid")
  }

  override def remove(fid: String): F[Unit] = {
    for {
      (volumeId, _) <- decomposeFid(fid)
      locationInfo <- protocol.location(volumeId)
      _ <- locationInfo.locations.traverse(protocol.remove(fid, _))
    } yield ()
  }
}

object CommandsInterpreter {

  private val decomposePattern = """(\d+),(.+)""".r
  def decomposeFid[F[_]](fid: String)(implicit ME: MonadThrow[F]): F[(String, String)] = {
    val iterator = decomposePattern.findAllIn(fid)
    for {
      volumeId <- ME.fromTry(Try(iterator.group(1)))
      cookie <- ME.fromTry(Try(iterator.group(2)))
    } yield (volumeId, cookie)
  }
}
