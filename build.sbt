import scalapb.compiler.Version.{grpcJavaVersion, scalapbVersion}

scalaVersion := "2.12.4"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime"       % scalapbVersion % "protobuf",
  // for gRPC
  "io.grpc"              %  "grpc-netty"            % grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc"  % scalapbVersion,
  // for JSON conversion
  "com.thesamet.scalapb" %% "scalapb-json4s"        % scalapbVersion,
  "com.pauldijou" %% "jwt-core" % "4.2.0",
  "com.auth0" % "jwks-rsa" % "0.11.0",
  "com.auth0" % "java-jwt" % "3.10.0",
)

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
