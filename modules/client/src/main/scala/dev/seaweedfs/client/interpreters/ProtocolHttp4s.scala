package dev.seaweedfs.client.interpreters

import cats.effect.{BracketThrow, Sync}
import cats.syntax.all._
import cats.{Applicative, Show}
import dev.seaweedfs.client.Protocol
import dev.seaweedfs.client.Protocol.{AssignInfo, WriteInfo}
import dev.seaweedfs.client.domain._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.{JsonDecoder, toMessageSynax}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.{Multipart, Part}

class ProtocolHttp4s[F[_]: JsonDecoder: BracketThrow: Logger] private (
  seaweedFSConfig: SeaweedFSConfig,
  client: Client[F]
) extends Protocol[F]
  with Http4sClientDsl[F] {

  import ProtocolHttp4s._

  private def runWithLog[A](request: Request[F])(func: Response[F] => F[A]): F[A] = {
    Logger[F].info(request.show) *> client.run(request).use { response =>
      Logger[F].info(response.show) *> func(response)
    }
  }

  override def getAssign: F[AssignInfo] = {
    for {
      uri <- Uri.fromString(s"http://${seaweedFSConfig.origin}/dir/assign").liftTo[F]
      request <- GET(uri)
      response <- runWithLog[AssignInfo](request) {
        case response if response.status === Status.Ok => response.asJsonDecode[AssignInfo]
        case response => ResponseError(response.status.code, response.status.reason).raiseError
      }
    } yield response
  }

  override def save(assignInfo: AssignInfo, photo: Photo): F[WriteInfo] = {
    for {
      header <- getHeader(photo.contentType)
      stream = fs2.Stream.emits(photo.body)
      multipart = Multipart[F](Vector(Part(Headers.of(header), stream)))
      uri <- Uri.fromString(s"${assignInfo.publicUrl}/${assignInfo.fid}").liftTo[F]
      request <- POST(multipart, uri)
      response <- runWithLog[WriteInfo](request.withHeaders(multipart.headers)) {
        case response if response.status == Status.Created => response.asJsonDecode[WriteInfo]
        case response => ResponseError(response.status.code, response.status.reason).raiseError
      }
    } yield response
  }
}

object ProtocolHttp4s {

  implicit def requestShow[F[_]]: Show[Request[F]] = request => {
    import request._
    s"$httpVersion $method $uri $headers $body"
  }

  implicit def responseShow[F[_]]: Show[Response[F]] = response => {
    import response._
    s"$httpVersion ${status.code} ${status.reason} $headers $body"
  }

  def getHeader[F[_]: Applicative](contentType: ContentType): F[`Content-Type`] = contentType match {
    case ContentType.Gif => `Content-Type`(MediaType.image.gif).pure[F]
    case ContentType.Jpeg => `Content-Type`(MediaType.image.jpeg).pure[F]
    case ContentType.Png => `Content-Type`(MediaType.image.png).pure[F]
    case ContentType.Tiff => `Content-Type`(MediaType.image.tiff).pure[F]
  }

  def make[F[_]: Sync](
    seaweedFSConfig: SeaweedFSConfig,
    client: Client[F]
  ): F[Protocol[F]] = {
    Slf4jLogger.create[F].flatMap { implicit logger =>
      Sync[F].delay(new ProtocolHttp4s[F](seaweedFSConfig, client))
    }
  }
}
