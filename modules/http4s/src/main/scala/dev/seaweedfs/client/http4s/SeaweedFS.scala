package dev.seaweedfs.client.http4s

import cats.effect.{ConcurrentEffect, Resource, Sync}
import dev.seaweedfs.client.Commands
import dev.seaweedfs.client.domain.SeaweedFSConfig
import dev.seaweedfs.client.interpreters.CommandsInterpreter
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

object SeaweedFS {

  def make[F[_]: ConcurrentEffect: Logger](
    seaweedFSConfig: SeaweedFSConfig
  )(implicit context: ExecutionContext): Resource[F, Commands[F]] = {
    for {
      client <- BlazeClientBuilder[F](context).resource
      protocol <- Resource.eval(Sync[F].delay(new ProtocolHttp4s[F](seaweedFSConfig, client)))
      commands <- Resource.eval(Sync[F].delay(new CommandsInterpreter[F](protocol)))
    } yield commands
  }
}
