package dev.seaweedfs.client

import dev.seaweedfs.client.Protocol.{AssignInfo, Location, LocationInfo, WriteInfo}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import java.io.File
import scala.concurrent.duration.FiniteDuration

trait Protocol[F[_]] {
  def getAssign(ttl: Option[FiniteDuration]): F[AssignInfo]
  def save(assignInfo: AssignInfo, file: File, ttl: Option[FiniteDuration]): F[WriteInfo]
  def location(volumeId: String): F[LocationInfo]
  def remove(fid: String, location: Location): F[Unit]
  def extract(fid: String, location: Location): F[File]
}

object Protocol {
  case class AssignInfo(count: Int, fid: String, url: String, publicUrl: String)
  case class WriteInfo(size: Long, eTag: String, name: String)
  case class Location(publicUrl: String, url: String)
  case class LocationInfo(volumeId: Int, locations: List[Location])

  implicit val assignResultDecoder: Decoder[AssignInfo] = deriveDecoder[AssignInfo]
  implicit val writeInfoDecoder: Decoder[WriteInfo] = deriveDecoder[WriteInfo]
  implicit val locationDecoder: Decoder[Location] = deriveDecoder[Location]
  implicit val locationInfoDecoder: Decoder[LocationInfo] = deriveDecoder[LocationInfo]
}
