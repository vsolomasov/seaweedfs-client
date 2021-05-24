package dev.seaweedfs.client

import dev.seaweedfs.client.Commands.PhotoInfo
import dev.seaweedfs.client.Protocol.{AssignInfo, WriteInfo}

import java.io.File

trait Commands[F[_]] {
  def save(file: File): F[PhotoInfo]
  def search(fid: String): F[Option[String]]
  def remove(fid: String): F[Unit]
}

object Commands {
  case class PhotoInfo(fid: String, size: Long, eTag: String, name: String)

  object PhotoInfo {
    def apply(assignInfo: AssignInfo, writeInfo: WriteInfo): PhotoInfo = new PhotoInfo(
      fid = assignInfo.fid,
      size = writeInfo.size,
      eTag = writeInfo.eTag,
      name = writeInfo.name
    )
  }
}
