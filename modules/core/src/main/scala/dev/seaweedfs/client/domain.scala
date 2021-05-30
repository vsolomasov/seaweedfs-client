package dev.seaweedfs.client

object domain {

  case class SeaweedFSConfig(schema: String, host: String, port: Int, usePublicUrl: Boolean = false) {
    val origin: String = s"$schema://$host:$port"
  }

  case class ResponseError(code: Int, reason: String) extends Exception {
    override def getMessage: String = s"Code: $code, Reason: $reason"
  }
}
