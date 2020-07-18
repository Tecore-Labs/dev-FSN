FROM openjdk:8
ENV JAVA_OPTS=-XX:+UseContainerSupport 
ADD comrate-contact-service.jar ccns.jar 
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ccns.jar"]