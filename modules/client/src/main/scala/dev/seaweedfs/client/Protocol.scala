package dev.seaweedfs.client

import dev.seaweedfs.client.Protocol.{AssignInfo, WriteInfo}
import dev.seaweedfs.client.domain.Photo
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

trait Protocol[F[_]] {
  def getAssign: F[AssignInfo]
  def save(assignInfo: AssignInfo, photo: Photo): F[WriteInfo]
}

object Protocol {
  case class AssignInfo(count: Int, fid: String, url: String, publicUrl: String)
  case class WriteInfo(size: Long, eTag: String, mime: String)

  implicit val assignResultDecoder: Decoder[AssignInfo] = deriveDecoder[AssignInfo]
  implicit val writeInfoDecoder: Decoder[WriteInfo] = deriveDecoder[WriteInfo]
}
