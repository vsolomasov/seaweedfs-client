package dev.seaweedfs.client

import cats.effect.{ConcurrentEffect, Resource}
import dev.seaweedfs.client.domain.SeaweedFSConfig
import dev.seaweedfs.client.interpreters.{CommandsInterpreter, ProtocolHttp4s}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

object SeaweedFS {

  def make[F[_]: ConcurrentEffect](
    seaweedFSConfig: SeaweedFSConfig
  )(context: ExecutionContext): Resource[F, Commands[F]] = {
    for {
      client <- BlazeClientBuilder[F](context).resource
      protocol <- Resource.eval(ProtocolHttp4s.make[F](seaweedFSConfig, client))
      commands <- Resource.eval(CommandsInterpreter.make[F](protocol))
    } yield commands
  }
}
