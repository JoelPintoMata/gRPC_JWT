package io.grpc.authentication

import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}

import io.grpc.authentication.AuthenticatorGrpc
import io.grpc.authentication.AuthenticatorGrpc.AuthenticatorBlockingStub
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.{ManagedChannel, StatusRuntimeException}
import io.grpc.netty.{NegotiationType, NettyChannelBuilder, NettyChannelProvider}
import pdi.jwt.{Jwt, JwtAlgorithm}


object AuthenticationClient {
  private[this] val logger = Logger.getLogger(classOf[AuthenticationClient].getName)

  def apply(host: String, port: Int): AuthenticationClient = {
    val channel = NettyChannelBuilder.forAddress(host, port)
      .nameResolverFactory(new DnsNameResolverProvider())
//      implement TLS
      .negotiationType(NegotiationType.PLAINTEXT)
      .build()

    val token = BearerToken(getJwt)
    logger.info("Client calling with token: " + token)
    val blockingStub = AuthenticatorGrpc.blockingStub(channel).withCallCredentials(token)

    new AuthenticationClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = AuthenticationClient("172.17.0.1", 50051)
    try {
      val user = args.headOption.getOrElse("world")
      client.authenticate(user)
    } finally {
      client.shutdown()
    }
  }

  private def getJwt: String = {
    Jwt.encode("Authorization client", Constants.JWT_SIGNING_KEY, JwtAlgorithm.HS256)
  }
}

class AuthenticationClient private(
    private val channel: ManagedChannel,
    private val blockingStub: AuthenticatorBlockingStub
) {
  private[this] val logger = Logger.getLogger(classOf[AuthenticationClient].getName)

  def shutdown(): Unit = {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    } catch {
      case e: InterruptedException =>
        logger.log(Level.SEVERE, "Could not shutdown the channel properly: {0}", e.getMessage)
    }
  }

  def authenticate(name: String): Unit = {
    val request = AuthenticationRequest(name = name)

    try {
      logger.info("Will try to authenticate, this should work")
      val response = blockingStub.authenticate(request)
      logger.log(Level.INFO, "Result: {0}", response)
    } catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }

    try {
      logger.info("Will try to authenticate without token")
      val blockingStubAux = blockingStub.withCallCredentials(null)
      blockingStubAux.authenticate(request)
    } catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }

    try {
      logger.info("Will try to authenticate with wrong")
      val token = BearerToken("some wrong token")
      val blockingStubAux = blockingStub.withCallCredentials(token)
      blockingStubAux.authenticate(request)
    } catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }
  }
}
