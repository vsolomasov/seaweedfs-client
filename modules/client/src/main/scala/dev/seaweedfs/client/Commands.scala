package dev.seaweedfs.client

import dev.seaweedfs.client.Protocol.WriteInfo
import dev.seaweedfs.client.domain.Photo

trait Commands[F[_]] {
  def save(photo: Photo): F[WriteInfo]
}
