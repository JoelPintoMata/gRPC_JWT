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
  // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt
  "io.jsonwebtoken" % "jjwt" % "0.9.1",
)

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
