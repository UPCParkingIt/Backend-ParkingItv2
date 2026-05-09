FROM openjdk:21-jdk-slim

WORKDIR /app

COPY .. .

RUN chmod +x mvnw
RUN ./mvnw clean package -pl parking-edge -am -DskipTests

CMD ["java", "-jar", "parking-edge/target/parking-edge-0.0.1-SNAPSHOT.jar"]