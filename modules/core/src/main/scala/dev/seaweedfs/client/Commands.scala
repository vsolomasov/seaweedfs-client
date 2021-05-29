package dev.seaweedfs.client

import dev.seaweedfs.client.Commands.FileInfo
import dev.seaweedfs.client.Protocol.{AssignInfo, WriteInfo}

import java.io.File
import scala.concurrent.duration.FiniteDuration

trait Commands[F[_]] {
  def save(file: File, ttl: Option[FiniteDuration]): F[FileInfo]
  def changeTtl(fid: String, ttl: Option[FiniteDuration]): F[FileInfo]
  def search(fid: String): F[Option[String]]
  def remove(fid: String): F[Unit]
}

object Commands {
  case class FileInfo(fid: String, size: Long, eTag: String, name: String)

  object FileInfo {
    def apply(assignInfo: AssignInfo, writeInfo: WriteInfo): FileInfo = new FileInfo(
      fid = assignInfo.fid,
      size = writeInfo.size,
      eTag = writeInfo.eTag,
      name = writeInfo.name
    )
  }
}
