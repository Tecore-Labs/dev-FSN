FROM openjdk:8
ENV JAVA_OPTS=-XX:+UseContainerSupport 
ADD Post-Service.jar cpms.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "cpms.jar"]