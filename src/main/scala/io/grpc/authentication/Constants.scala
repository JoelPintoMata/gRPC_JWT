package io.grpc.authentication

import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER

object Constants {
  val BEARER_TYPE = "Bearer"
  val AUTHORIZATION_METADATA_KEY: Metadata.Key[String] = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER)
}