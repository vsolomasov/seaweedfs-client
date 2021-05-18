package dev.seaweedfs.client.interpreters

import cats.Monad
import cats.effect.Sync
import cats.syntax.all._
import dev.seaweedfs.client.Protocol.WriteInfo
import dev.seaweedfs.client.{Commands, Protocol, domain}

class CommandsInterpreter[F[_]: Monad] private (
  protocol: Protocol[F]
) extends Commands[F] {

  override def save(photo: domain.Photo): F[WriteInfo] =
    protocol.getAssign.flatMap(protocol.save(_, photo))
}

object CommandsInterpreter {

  def make[F[_]: Sync](protocol: Protocol[F]): F[Commands[F]] =
    Sync[F].delay(new CommandsInterpreter[F](protocol))
}
