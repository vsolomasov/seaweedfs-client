package dev.seaweedfs.client.http4s

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Sync}
import dev.seaweedfs.client.Commands
import dev.seaweedfs.client.domain.SeaweedFSConfig
import dev.seaweedfs.client.http4s.file.FileUtilInterpreter
import dev.seaweedfs.client.interpreters.CommandsInterpreter
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

object SeaweedFS {

  def make[F[_]: ConcurrentEffect: ContextShift](
    seaweedFSConfig: SeaweedFSConfig
  )(context: ExecutionContext, blocker: Blocker): Resource[F, Commands[F]] = {
    for {
      client <- BlazeClientBuilder[F](context).resource
      fileUtils <- Resource.eval(FileUtilInterpreter.make[F](blocker))
      protocol <- Resource.eval(ProtocolHttp4s.make[F](seaweedFSConfig, client, fileUtils))
      commands <- Resource.eval(Sync[F].delay(new CommandsInterpreter[F](protocol)))
    } yield commands
  }
}
