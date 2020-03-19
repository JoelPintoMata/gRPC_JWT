package io.grpc.authentication

import java.util.logging.Logger

import io.grpc._
import pdi.jwt.{Jwt, JwtAlgorithm}


class AuthenticationServerInterceptor extends ServerInterceptor {
  private[this] val logger = Logger.getLogger(classOf[AuthenticationServerInterceptor].getName)

  def interceptCall[ReqT, RespT](serverCall: ServerCall[ReqT, RespT], metadata: Metadata, serverCallHandler: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    val value = metadata.get(Constants.AUTHORIZATION_METADATA_KEY)
    logger.info("Intercepted metadata: " + metadata.toString())
    logger.info("Intercepted value: " + value)

    var status = Status.OK

    if (value == null) {
      status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing")
      logger.warning("Authentication failed: " + status)
    } else if (!value.startsWith(Constants.BEARER_TYPE)) {
      status = Status.UNAUTHENTICATED.withDescription("Unknown authorization type")
      logger.warning("Authentication failed: " + status)
    } else {
      val token = value.substring(Constants.BEARER_TYPE.length).trim

      if (!Jwt.isValid(token, scala.util.Properties.envOrElse("JWT_SIGNING_KEY", ""), Seq(JwtAlgorithm.HS256))) {
        status = Status.UNAUTHENTICATED.withDescription("Invalid token")
        logger.warning("Authentication token:" + token + " failed: " + status)
      }
    }

    if (status.getCode == Status.UNAUTHENTICATED.getCode) {
      serverCall.close(status, metadata)
      new ServerCall.Listener[ReqT]() { // noop
      }
    } else {
      logger.info("Authentication successful")
      val ctx = Context.current
      Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler)
    }
  }
}