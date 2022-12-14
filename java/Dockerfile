FROM ubuntu:16.04

# INSTALL OPENJDK
RUN apt-get update
RUN apt-get update && apt-get -y install software-properties-common
RUN apt-get update && apt-get -y install wget apt-transport-https gnupg curl emacs

RUN wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add -
RUN add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
RUN apt update && apt-get -y install adoptopenjdk-11-hotspot build-essential swig python unzip build-essential swig python unzip
RUN update-alternatives --set java /usr/lib/jvm/adoptopenjdk-11-hotspot-amd64/bin/java
ENV JAVA_HOME /usr/lib/jvm/adoptopenjdk-11-hotspot-amd64/

# MVN INSTALL FOR JAVA
# get maven 3.3.9
RUN wget --no-verbose -O /tmp/apache-maven-3.3.9.tar.gz http://archive.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz

# verify checksum
RUN echo "516923b3955b6035ba6b0a5b031fbd8b /tmp/apache-maven-3.3.9.tar.gz" | md5sum -c

# install maven
RUN tar xzf /tmp/apache-maven-3.3.9.tar.gz -C /opt/
RUN ln -s /opt/apache-maven-3.3.9 /opt/maven
RUN ln -s /opt/maven/bin/mvn /usr/local/bin
RUN rm -f /tmp/apache-maven-3.3.9.tar.gz
ENV MAVEN_HOME /opt/maven

RUN mkdir ./example
RUN chown -R example:example ./example

# prevent download of maven dependencies with source code change
COPY pom.xml .
# To resolve dependencies in a safe way (no re-download when the source code changes)
RUN mvn clean install -Dmaven.main.skip -Dmaven.test.skip && rm -r target
# copy the code
COPY src/ ./example/src
COPY pom.xml ./example

#compile codebase
RUN mvn -f ./example/pom.xml clean install

WORKDIR /home/example

ENTRYPOINT ["mvn", "spring-boot:run"]