package io.grpc.helloworld

import io.grpc.{Attributes, CallCredentials, Metadata, MethodDescriptor, Status}


case class BearerToken(value: String) extends CallCredentials {

  import java.util.concurrent.Executor

  override def applyRequestMetadata(method: MethodDescriptor[_, _], attrs: Attributes, executor: Executor, metadataApplier: CallCredentials.MetadataApplier): Unit = {
   executor.execute(() => {
      try {
        val headers = new Metadata()
        headers.put(Constants.AUTHORIZATION_METADATA_KEY, String.format("%s %s", Constants.BEARER_TYPE, value))
        metadataApplier.apply(headers)
      } catch {
        case e: Throwable =>
          metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e))
      }
    })
  }

  override def thisUsesUnstableApi(): Unit = {
    // noop
  }
}
