#docker run -d \
#-p 1414:1414 \
#--name mq \
#mq

docker run -d \
--publish 1414:1414 --publish 9443:9443 \
--env LICENSE=accept \
--env MQ_QMGR_NAME=QM1 \
--name mq \
--env MQ_APP_PASSWORD=passw0rd \
ibmcom/mq:latest