package io.grpc.authentication

import java.time.{LocalDateTime, Period, ZoneOffset}
import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}

import io.grpc.authentication.AuthenticatorGrpc.AuthenticatorBlockingStub
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
import io.grpc.{ManagedChannel, StatusRuntimeException}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}


object AuthenticationClient {
  private[this] val logger = Logger.getLogger(classOf[AuthenticationClient].getName)

  def apply(host: String, port: Int): AuthenticationClient = {
    val channel = NettyChannelBuilder.forAddress(host, port)
      .nameResolverFactory(new DnsNameResolverProvider())
//      TODO implement TLS
      .negotiationType(NegotiationType.PLAINTEXT)
      .build()

    val token = BearerToken(getJwt)
    logger.info("Client calling with token: " + token)
    val blockingStub = AuthenticatorGrpc.blockingStub(channel).withCallCredentials(token)

    new AuthenticationClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = AuthenticationClient(args(0), Integer.valueOf(args(1)))
    try {
      client.authenticate(args.headOption.getOrElse("Authenticate me"))
    } finally {
      client.shutdown()
    }
  }

  private def getJwt: String = {
    Jwt.encode(JwtClaim().withContent("Authentication client"), Constants.JWT_SIGNING_KEY, JwtAlgorithm.HS256)
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
      logger.info("Will try to authenticate with wrong token")
      val token = BearerToken("some wrong token")
      val blockingStubAux = blockingStub.withCallCredentials(token)
      blockingStubAux.authenticate(request)
    } catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }

    try {
      logger.info("Will try to authenticate expired token")
      logger.info("First, prove that it works")
      var jwt = Jwt.encode(JwtClaim().withContent("Authentication client"), Constants.JWT_SIGNING_KEY, JwtAlgorithm.HS256)
      var token = BearerToken(jwt)
      var blockingStubAux = blockingStub.withCallCredentials(token)
      val response = blockingStubAux.authenticate(request)
      logger.log(Level.INFO, "Result: {0}", response)

      logger.info("Now, same token but with expired claim")
      val clock = LocalDateTime.now().minus(Period.ofDays(1));
      jwt = Jwt.encode(JwtClaim().withContent("Authentication client").expiresAt(clock.toEpochSecond(ZoneOffset.UTC)), Constants.JWT_SIGNING_KEY, JwtAlgorithm.HS256)
      token = BearerToken(jwt)
      blockingStubAux = blockingStub.withCallCredentials(token)
      blockingStubAux.authenticate(request)
    } catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }
  }
}
