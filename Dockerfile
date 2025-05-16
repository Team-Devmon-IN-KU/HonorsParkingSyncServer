FROM amazoncorretto:8

# TLSv1, TLSv1.1 허용을 위해 java.security 수정
RUN sed -i 's/jdk.tls.disabledAlgorithms=.*/jdk.tls.disabledAlgorithms=SSLv3, RC4, DH keySize < 1024/' $JAVA_HOME/jre/lib/security/java.security

ENV SPRING_PROFILES_ACTIVE=prod
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1,TLSv1.1,TLSv1.2", "-jar", "app.jar"]