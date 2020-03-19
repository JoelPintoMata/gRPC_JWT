FROM openjdk:8-jre-alpine

# Make port 50051 available to the world outside this container
EXPOSE 50051

COPY target/scala-2.12/scala-grpc-helloworld-assembly-0.1.0-SNAPSHOT.jar /

# Start the application
CMD java -cp scala-grpc-helloworld-assembly-0.1.0-SNAPSHOT.jar io.grpc.authentication.AuthenticationClient 172.17.0.1 50051