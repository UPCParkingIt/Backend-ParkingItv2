FROM openjdk:21-jdk-slim

WORKDIR /app

COPY .. .

RUN chmod +x mvnw
RUN ./mvnw clean package -pl parking-cloud -am -DskipTests

CMD ["java", "-jar", "parking-cloud/target/parking-cloud-0.0.1-SNAPSHOT.jar"]