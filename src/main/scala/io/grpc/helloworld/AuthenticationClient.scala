package io.grpc.helloworld

import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}

import io.grpc.helloworld.AuthenticatorGrpc.AuthenticatorBlockingStub
import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}
import pdi.jwt.{Jwt, JwtAlgorithm}


object AuthenticationClient {
  private[this] val logger = Logger.getLogger(classOf[AuthenticationClient].getName)

  def apply(host: String, port: Int): AuthenticationClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port)
      .usePlaintext(true).
      build

    val token = BearerToken(getJwt)
    logger.info("Client calling with token: " + token)
    val blockingStub = AuthenticatorGrpc.blockingStub(channel).withCallCredentials(token)

    new AuthenticationClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = AuthenticationClient("localhost", 50051)
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
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
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
