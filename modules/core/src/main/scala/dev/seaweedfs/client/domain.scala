package dev.seaweedfs.client

object domain {

  case class SeaweedFSConfig(host: String, port: Int) {
    val origin: String = s"$host:$port"
  }

  case class ResponseError(code: Int, reason: String) extends Exception {
    override def getMessage: String = s"Code: $code, Reason: $reason"
  }
}
