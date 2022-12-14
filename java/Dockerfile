FROM openjdk:8-jdk-alpine

COPY .mvn .
COPY ./mvnw .
RUN chmod ug+x mvnw

COPY ./pom.xml .

RUN mvnw dependency:resolve
RUN mvnw clean package

ENTRYPOINT ["java","-jar","/target/ websockets-0.0.1-SNAPSHOT.jar"]