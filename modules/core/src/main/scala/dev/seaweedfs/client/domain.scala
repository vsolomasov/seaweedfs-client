package dev.seaweedfs.client

object domain {

  case class SeaweedFSConfig(host: String, port: Int) {
    val origin: String = s"$host:$port"
  }

  case class ResponseError(code: Int, reason: String) extends Exception {
    override def getMessage: String = s"Code: $code, Reason: $reason"
  }

  sealed trait ContentType
  object ContentType {
    case object Jpeg extends ContentType
    case object Png extends ContentType
    case object Gif extends ContentType
    case object Tiff extends ContentType
  }

  case class Photo(name: String, contentType: ContentType, body: Array[Byte])
}
