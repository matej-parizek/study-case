FROM eclipse-temurin:17-jre
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
WORKDIR /app
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
