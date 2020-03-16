package io.grpc.helloworld

import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}

import io.grpc.helloworld.GreeterGrpc.GreeterBlockingStub
import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}
import pdi.jwt.{Jwt, JwtAlgorithm}


object HelloWorldClient {
  private[this] val logger = Logger.getLogger(classOf[HelloWorldClient].getName)

  def apply(host: String, port: Int): HelloWorldClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port)
      .usePlaintext(true).
      build

    val token = BearerToken(getJwt)
    logger.info("Client calling with token: " + token)
    val blockingStub = GreeterGrpc.blockingStub(channel).withCallCredentials(token)

    new HelloWorldClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = HelloWorldClient("localhost", 50051)
    try {
      val user = args.headOption.getOrElse("world")
      client.greet(user)
    } finally {
      client.shutdown()
    }
  }

  private def getJwt: String = {
    Jwt.encode("Autorization client", Constants.JWT_SIGNING_KEY, JwtAlgorithm.HS256)
  }
}

class HelloWorldClient private (
    private val channel: ManagedChannel,
    private val blockingStub: GreeterBlockingStub
) {
  private[this] val logger = Logger.getLogger(classOf[HelloWorldClient].getName)

  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  /** Say hello to server. */
  def greet(name: String): Unit = {
    logger.info("Will try to greet " + name + " ...")
    val request = HelloRequest(name = name)
    try {
      val response = blockingStub.sayHello(request)
      logger.info("Greeting: " + response.message)
    } catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }
  }
}
