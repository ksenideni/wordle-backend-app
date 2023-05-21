FROM maven:3.8.5-openjdk-18 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -Dmaven.test.skip.exec=true

FROM openjdk:18
COPY --from=build /home/app/target/wordle-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar
EXPOSE 8080
CMD ["java", "-Dspring.profiles.active=production","-jar", "/usr/local/lib/app.jar"]