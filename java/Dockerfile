FROM openjdk:8-jdk-alpine

COPY .mvn/ .mvn
COPY ./mvnw .
RUN chmod ug+x mvnw
COPY ./pom.xml .
RUN ./mvnw verify --fail-never

COPY src src/
RUN ./mvnw clean package

#ENTRYPOINT ["./mvnw", "spring-boot:run"]
ENTRYPOINT ["java","-jar","/target/websockets-0.0.1-SNAPSHOT.jar"]