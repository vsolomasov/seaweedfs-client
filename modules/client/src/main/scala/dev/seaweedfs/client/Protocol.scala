package dev.seaweedfs.client

import dev.seaweedfs.client.Protocol.{AssignInfo, Location, LocationInfo, WriteInfo}
import dev.seaweedfs.client.domain.Photo
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

trait Protocol[F[_]] {
  def getAssign: F[AssignInfo]
  def save(assignInfo: AssignInfo, photo: Photo): F[WriteInfo]
  def location(volumeId: String): F[LocationInfo]
  def remove(fid: String, location: Location): F[Unit]
}

object Protocol {
  case class AssignInfo(count: Int, fid: String, url: String, publicUrl: String)
  case class WriteInfo(size: Long, eTag: String, mime: String)
  case class Location(publicUrl: String, url: String)
  case class LocationInfo(volumeId: Int, locations: List[Location])

  implicit val assignResultDecoder: Decoder[AssignInfo] = deriveDecoder[AssignInfo]
  implicit val writeInfoDecoder: Decoder[WriteInfo] = deriveDecoder[WriteInfo]
  implicit val locationDecoder: Decoder[Location] = deriveDecoder[Location]
  implicit val locationInfoDecoder: Decoder[LocationInfo] = deriveDecoder[LocationInfo]
}
