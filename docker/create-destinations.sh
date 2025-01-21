#!/bin/bash

# Wait until ActiveMQ Artemis is fully started
sleep 10

curl -u admin:admin -X POST -d "name=message.queue" http://localhost:8161/api/jms/queues
curl -u admin:admin -X POST -d "name=group.queue" http://localhost:8161/api/jms/queues
curl -u admin:admin -X POST -d "name=message.topic" http://localhost:8161/api/jms/topics
