# Java 21 환경 설정
FROM eclipse-temurin:21-jdk

# 작업할 폴더 제작
WORKDIR /app

# 프로젝트의 모든 파일을 Docker 안으로 복사
COPY . .

# 권한을 주고 Gradle로 빌드를 실행
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 빌드된 JAR 파일을 app.jar로 이름을 변경
RUN cp build/libs/*SNAPSHOT.jar app.jar

# 서버 실행
ENTRYPOINT ["java", "-jar", "app.jar"]