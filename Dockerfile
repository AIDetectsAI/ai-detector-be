#it's not the smallest possible image, but alpine linux because of musl libc can cause problems with native C/C++ libs
FROM eclipse-temurin:22-jre
VOLUME /tmp
COPY target/ai-detector-be-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]