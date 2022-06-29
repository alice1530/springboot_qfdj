FROM centos:latest

WORKDIR /qfdj

COPY  qfdj-v1.0.jar /qfdj
COPY  application.yml /qfdj

ADD ffmpeg.tar.gz /qfdj
ADD jre8.tar.gz /usr/java/jdk

ENV JAVA_HOME /usr/java/jdk
ENV PATH ${PATH}:${JAVA_HOME}/bin
ENV LANG C.UTF-8

RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

EXPOSE 7780

ENTRYPOINT ["java","-jar","qfdj-v1.0.jar"]

