FROM jrottenberg/ffmpeg:3.4-ubuntu


RUN apt update && \
apt install -y python3 python3-pip curl gnupg

RUN python3 -m pip install torch torchvision &&  \
python3 -m pip install -U protobuf

#RabbitMQ, repo
RUN curl -fsSL https://github.com/rabbitmq/signing-keys/releases/download/2.0/rabbitmq-release-signing-key.asc | apt-key add - &&  \
apt-get install apt-transport-https 

COPY de.sh /home/de.sh
RUN /home/de.sh

#RUN tee /etc/apt/sources.list.d/bintray.rabbitmq.list <<EOF \
#deb https://dl.bintray.com/rabbitmq-erlang/debian bionic erlang \
#deb https://dl.bintray.com/rabbitmq/debian bionic main \
#EOF



#actual RMQ installation for python3
RUN apt-get update -y &&  \
apt-get install rabbitmq-server -y --fix-missing &&  \
python3 -m pip install pika --upgrade

#start RMQ, is not needed, will connect to host
#RUN service rabbitmq-server start

COPY PythonWorker /home/PythonWorker

ENTRYPOINT ["python3"]
#CMD "/home/PythonWorker/FrontConnector.py"
