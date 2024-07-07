FROM  gradle:8.8.0-jdk22-jammy
RUN mkdir /usr/src/myapp
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
#RUN gradle build
CMD ["gradle","bootRun","--no-daemon"]
