package dev.seaweedfs.client.http4s

import cats.Show
import cats.effect.{BracketThrow, Sync}
import cats.syntax.all._
import dev.seaweedfs.client.Protocol
import dev.seaweedfs.client.Protocol.{AssignInfo, Location, LocationInfo, WriteInfo}
import dev.seaweedfs.client.domain._
import dev.seaweedfs.client.http4s.file.FileUtil
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.{JsonDecoder, toMessageSynax}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.multipart.Multipart

import java.io.File
import scala.concurrent.duration.FiniteDuration

private[http4s] class ProtocolHttp4s[F[_]: JsonDecoder: BracketThrow: Logger] private (
  seaweedFSConfig: SeaweedFSConfig,
  client: Client[F],
  fileUtil: FileUtil[F]
) extends Protocol[F]
  with Http4sClientDsl[F] {

  import ProtocolHttp4s._

  private def runWithLog[A](request: Request[F])(func: Response[F] => F[A]): F[A] = {
    Logger[F].info(request.show) *> client.run(request).use { response =>
      Logger[F].info(response.show) *> func(response)
    }
  }

  override def getAssign(ttl: Option[FiniteDuration]): F[AssignInfo] = {
    for {
      rawUri <- Uri.fromString(s"${seaweedFSConfig.origin}/dir/assign").liftTo[F]
      uri = ttl.fold(rawUri)(ttl => rawUri.withQueryParam("ttl", s"${ttl.toMinutes}m"))
      request <- GET(uri)
      response <- runWithLog[AssignInfo](request) {
        case response if response.status === Status.Ok => response.asJsonDecode[AssignInfo]
        case response => ResponseError(response.status.code, response.status.reason).raiseError
      }
    } yield response
  }

  override def save(assignInfo: AssignInfo, file: File, ttl: Option[FiniteDuration]): F[WriteInfo] = {
    for {
      fileData <- fileUtil.createPart(file)
      multipart = Multipart[F](Vector(fileData))
      rawUri <- Uri.fromString(s"${assignInfo.getOrigin(seaweedFSConfig)}/${assignInfo.fid}").liftTo[F]
      uri = ttl.fold(rawUri)(ttl => rawUri.withQueryParam("ttl", s"${ttl.toMinutes}m"))
      request <- POST(multipart, uri)
      response <- runWithLog[WriteInfo](request.withHeaders(multipart.headers)) {
        case response if response.status == Status.Created => response.asJsonDecode[WriteInfo]
        case response => ResponseError(response.status.code, response.status.reason).raiseError
      }
    } yield response
  }

  override def location(volumeId: String): F[LocationInfo] = {
    for {
      uri <- Uri.fromString(s"${seaweedFSConfig.origin}/dir/lookup?volumeId=$volumeId").liftTo[F]
      request <- GET(uri)
      result <- runWithLog[LocationInfo](request) {
        case response if response.status == Status.Ok => response.asJsonDecode[LocationInfo]
        case response => ResponseError(response.status.code, response.status.reason).raiseError
      }
    } yield result
  }

  override def remove(fid: String, location: Location): F[Unit] = {
    for {
      uri <- Uri.fromString(s"${location.getOrigin(seaweedFSConfig)}/$fid").liftTo[F]
      request <- DELETE(uri)
      _ <- runWithLog[Unit](request) {
        case response if response.status == Status.Accepted => ().pure[F]
        case response => ResponseError(response.status.code, response.status.reason).raiseError[F, Unit]
      }
    } yield ()
  }

  override def extract(fid: String, location: Location): F[File] = {
    for {
      uri <- Uri.fromString(s"${location.getOrigin(seaweedFSConfig)}/$fid").liftTo[F]
      request <- GET(uri)
      file <- runWithLog[File](request) {
        case response if response.status == Status.Ok => fileUtil.save(response.body)
        case response => ResponseError(response.status.code, response.status.reason).raiseError
      }
    } yield file
  }
}

object ProtocolHttp4s {

  def make[F[_]: Sync](
    seaweedFSConfig: SeaweedFSConfig,
    client: Client[F],
    fileUtil: FileUtil[F]
  ): F[ProtocolHttp4s[F]] = Slf4jLogger.create[F].flatMap { implicit logger =>
    Sync[F].delay(new ProtocolHttp4s[F](seaweedFSConfig, client, fileUtil))
  }

  implicit def requestShow[F[_]]: Show[Request[F]] = request => {
    import request._
    s"$httpVersion $method $uri $headers $body"
  }

  implicit def responseShow[F[_]]: Show[Response[F]] = response => {
    import response._
    s"$httpVersion ${status.code} ${status.reason} $headers $body"
  }
}
