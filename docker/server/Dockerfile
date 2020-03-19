FROM openjdk:8-jre-alpine

# Make port 50051 available to the world outside this container
EXPOSE 50051

ENV JWT_SIGNING_KEY=L8hHXsaQOUjk5rg7XPGv4eL36anlCrkMz8CJ0i/8E/0=

COPY target/scala-2.12/scala-grpc-helloworld-assembly-0.1.0-SNAPSHOT.jar /

# Start the application
CMD java -cp scala-grpc-helloworld-assembly-0.1.0-SNAPSHOT.jar io.grpc.authentication.AuthenticationServer