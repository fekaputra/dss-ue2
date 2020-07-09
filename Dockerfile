FROM maven:3.5.2-jdk-8 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean install

FROM openjdk:8-jdk-alpine
COPY --from=build /usr/src/app/target/backend-0.0.1-SNAPSHOT.jar /usr/app/backend-0.0.1-SNAPSHOT.jar
EXPOSE 8080
RUN mkdir /target
ENTRYPOINT ["java","-jar","/usr/app/backend-0.0.1-SNAPSHOT.jar"]

# Partial credit of this dockerfile goes to: https://aboullaite.me/multi-stage-docker-java/
