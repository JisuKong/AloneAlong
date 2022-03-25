FROM openjdk:8-jre
COPY target/alonealong-boot.war app.war
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.war", "bee", "run", "&"]
