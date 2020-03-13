package io.grpc.helloworld

import java.util.logging.Logger

import io.grpc.{Context, Contexts, Metadata, ServerCall, ServerCallHandler, ServerInterceptor, Status}
import io.grpc.helloworld.{Constants, HelloWorldClient}
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts


class HelloWorldAuthorizationServerInterceptor extends ServerInterceptor {
  private[this] val logger = Logger.getLogger(classOf[HelloWorldAuthorizationServerInterceptor].getName)

  private val parser = Jwts.parser.setSigningKey(Constants.JWT_SIGNING_KEY)

  def interceptCall[ReqT, RespT](serverCall: ServerCall[ReqT, RespT], metadata: Metadata, serverCallHandler: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    val value = metadata.get(Constants.AUTHORIZATION_METADATA_KEY)
    logger.info("Intercepted metadata: " + metadata.toString())
    logger.info("Intercepted value: " + value)

    var status = Status.OK

    if (value == null) {
      status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing")
    } else if (!value.startsWith(Constants.BEARER_TYPE)) {
      status = Status.UNAUTHENTICATED.withDescription("Unknown authorization type")
    } else {
      try {
        val token = value.substring(Constants.BEARER_TYPE.length).trim
        logger.info("Intercepted token: " + token)

        val claims = parser.parseClaimsJws(token)
        val ctx = Context.current.withValue(Constants.CLIENT_ID_CONTEXT_KEY, claims.getBody.getSubject)
        return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler)
      } catch {
        case e: Exception =>
          status = Status.UNAUTHENTICATED.withDescription(e.getMessage).withCause(e)
      }
    }
    serverCall.close(status, metadata)
    new ServerCall.Listener[ReqT]() { // noop
    }
  }
}