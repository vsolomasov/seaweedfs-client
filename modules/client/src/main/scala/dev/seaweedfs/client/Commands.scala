package dev.seaweedfs.client

import dev.seaweedfs.client.Commands.PhotoInfo
import dev.seaweedfs.client.Protocol.{AssignInfo, WriteInfo}
import dev.seaweedfs.client.domain.Photo

trait Commands[F[_]] {
  def save(photo: Photo): F[PhotoInfo]
  def search(fid: String): F[Option[String]]
  def remove(fid: String): F[Unit]
}

object Commands {
  case class PhotoInfo(fid: String, size: Long, eTag: String, mime: String)

  object PhotoInfo {
    def apply(assignInfo: AssignInfo, writeInfo: WriteInfo): PhotoInfo = new PhotoInfo(
      fid = assignInfo.fid,
      size = writeInfo.size,
      eTag = writeInfo.eTag,
      mime = writeInfo.mime
    )
  }
}
