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
//  "io.jsonwebtoken" % "jjwt" % "0.9.1",
  "com.pauldijou" %% "jwt-core" % "4.2.0",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "com.auth0" % "jwks-rsa" % "0.11.0",
  // https://mvnrepository.com/artifact/com.auth0/java-jwt
  "com.auth0" % "java-jwt" % "3.10.0",


)

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
