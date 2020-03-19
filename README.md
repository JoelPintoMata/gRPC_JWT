# Scala-gRPC-Authentication-Service

A scala authentication service based on gRPC

## Whats inside?
__Server__: the `AuthenticationServer` package contains the grpc server implementation
__Client__: the `AuthenticationClient` package exemplifies a typical grpc client + a main class responsible for firing "integration tests" against a running service (see bellow how to setup).

## How to use?

### Docker

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