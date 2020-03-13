package io.grpc.helloworld

import java.util.logging.Logger

import com.sun.net.httpserver.Headers
import io.grpc.{Server, ServerBuilder}
import io.grpc.helloworld.{GreeterGrpc, HelloReply, HelloRequest}

import scala.concurrent.{ExecutionContext, Future}

/**
  * [[https://github.com/grpc/grpc-java/blob/v0.15.0/examples/src/main/java/io/grpc/examples/helloworld/HelloWorldServer.java]]
  */
object HelloWorldServer {
  private val logger = Logger.getLogger(classOf[HelloWorldServer].getName)

  def main(args: Array[String]): Unit = {
    val server = new HelloWorldServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }

  private val port = 50051
}

class HelloWorldServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null

  private def start(): Unit = {
    server = ServerBuilder
      .forPort(HelloWorldServer.port)
      .addService(GreeterGrpc.bindService(new HelloWorldService, executionContext))
      .intercept(new HelloWorldAuthorizationServerInterceptor())
      .build
      .start
    HelloWorldServer.logger.info(
      "Server started, listening on " + HelloWorldServer.port)
    sys.addShutdownHook {
      System.err.println(
        "*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
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

  private class HelloWorldService extends GreeterGrpc.Greeter {
    override def sayHello(req: HelloRequest) = {
      val reply = HelloReply(message = "Hello " + req.name)
      Future.successful(reply)
    }
  }
}
