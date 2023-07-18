#######################
##       BUILD       ##
#######################

# Use the maven base image with jdk for compile
FROM  maven:3.6.3-adoptopenjdk-11 as builder 

# The page says that this part will increase the maven speed. Need test.
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Set a working directory
WORKDIR /webFlux

# Copy the POM.XML  - this part comes first due the inmutable (almost none) state of this file
COPY pom.xml .

# go-offline - Download al the dependencies in pom.xml
RUN mvn dependency:go-offline

# copy your other files
COPY ./src ./src

# compile the source code and package it in a jar file
RUN mvn clean install -Dmaven.test.skip=true

#######################
##       Develop     ##
#######################

# For production, we only need to use the jre image, so we can reduce the image size by not using jdk
FROM openjdk:11 as develop

WORKDIR /webFlux

COPY --from=builder /webFlux /webFlux

EXPOSE 8080




#######################
##     Production    ##
#######################

# For production, we only need to use the jre image, so we can reduce the image size by not using jdk
FROM adoptopenjdk/openjdk11:jre-11.0.9_11-alpine as production

WORKDIR /webFlux

# Copy from builder stage the jar file
COPY --from=builder /webFlux/target/mvc.jar /webFlux

# Execute the jar with the entry point
ENTRYPOINT [ "java", "-jar", "camel-0.1.0.jar" ]