FROM  eclipse-temurin:22
RUN mkdir /usr/src/myapp
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN ./gradlew build
CMD ["./gradlew","bootRun"]
