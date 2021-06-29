FROM jrottenberg/ffmpeg:3.4-ubuntu


RUN apt update && \
apt install -y python3 python3-pip curl gnupg apt-transport-https --no-install-recommends

#RUN python3 -m pip install torch torchvision &&  \
RUN python3 -m pip install -U protobuf

#RabbitMQ
RUN curl -1sLf \
  'https://dl.cloudsmith.io/public/rabbitmq/rabbitmq-server/setup.deb.sh' \
  | bash


RUN python3 -m pip install pika --upgrade

#make image small
RUN rm -rf /var/lib/apt/lists/*

#start RMQ, is not needed, will connect to host
#RUN service rabbitmq-server start

COPY PythonWorker /home/PythonWorker

ENTRYPOINT ["python3"]
#CMD "/home/PythonWorker/FrontConnector.py"

