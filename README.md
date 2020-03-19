# Scala-gRPC-Authentication-Service

A scala authentication service based on gRPC

## Whats inside?
__Server__: the `AuthenticationServer` package contains the grpc server implementation
__Client__: the `AuthenticationClient` package exemplifies a typical grpc client + a main class responsible for firing "integration tests" against a running service (see bellow how to setup).

## How to use?

First, run
```
sbt clean compile "runMain io.grpc.authentication.AuthenticationServer"
```

Second, run
```
sbt clean compile "runMain io.grpc.authentication.AuthenticationClient"
```

## Docker

Create the jar file. On the sbt console
```
assembly
```

docker network create my-network

Build
```
docker build -f docker/server/Dockerfile -t server .
docker build -f docker/client/Dockerfile -t client .
```

Run
```
docker network create mynetwork      
docker run --net=mynetwork -p 50051:50051 -ti server
docker run --net=mynetwork -ti client
```

## Support links
https://www.jsonwebtoken.io
https://sultanov.dev/blog/securing-java-grpc-services-with-jwt-based-authentication/
https://github.com/AnarSultanov/examples/search?q=Constants.JWT_SIGNING_KEY&unscoped_q=Constants.JWT_SIGNING_KEY
https://github.com/avast/grpc-java-jwt

https://github.com/pauldijou/jwt-scala/blob/master/docs/src/main/tut/_includes/tut/jwt-core-jwt.md
https://www.programcreek.com/scala/pdi.jwt.JwtAlgorithm