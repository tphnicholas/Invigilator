FROM maven:3-jdk-11 AS build
COPY pom.xml /pom.xml
COPY src /src/
RUN mvn clean package -f /pom.xml

FROM openjdk:11
ENV BOT_TOKEN=UNSET
RUN mkdir /config/
COPY --from=build /target/Invigilator-jar-with-dependencies.jar /Invigilator.jar
CMD java -jar /Invigilator.jar $BOT_TOKEN