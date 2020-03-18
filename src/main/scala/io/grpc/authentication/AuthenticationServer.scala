package io.grpc.authentication

import java.util.logging.Logger

import io.grpc.Server
import io.grpc.authentication.{AuthenticationRequest, AuthenticatorGrpc}
import io.grpc.netty.NettyServerBuilder

import scala.concurrent.{ExecutionContext, Future}


object AuthenticationServer {
  private val logger = Logger.getLogger(classOf[AuthenticationServer].getName)

  private val port = 50051

  def main(args: Array[String]): Unit = {
    val server = new AuthenticationServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class AuthenticationServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null

  private def start(): Unit = {
    server = NettyServerBuilder
      .forPort(AuthenticationServer.port)
      .addService(AuthenticatorGrpc.bindService(new AuthenticationServer(), executionContext))
      .intercept(new AuthenticationServerInterceptor())
      .build
      .start
    AuthenticationServer.logger.info("Server started, listening on " + AuthenticationServer.port)
    sys.addShutdownHook {
      System.err.println("Shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("Server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class AuthenticationServer extends AuthenticatorGrpc.Authenticator {
    override def authenticate(req: AuthenticationRequest) = {
      val reply = AuthenticationReply(message = req.name + " : authenticated!")
      Future.successful(reply)
    }
  }
}
