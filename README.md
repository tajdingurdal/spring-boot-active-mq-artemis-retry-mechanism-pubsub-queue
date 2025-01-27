# Spring Boot ActiveMQ Artemis Integration

A messaging system implementing queues, topics, and retry mechanisms using Spring Boot and ActiveMQ Artemis.

## Features
- Queue and Topic message handling
- Retry mechanism with Dead Letter Queue (DLQ)
- Message expiration and auditing
- PostgreSQL integration for message tracking
- Scheduled message processing
- REST API for message publishing

## Tech Stack
- Java 17
- Spring Boot 3.1.1
- ActiveMQ Artemis
- PostgreSQL
- Maven
- Docker

## Quick Start

1. **Prerequisites**
    - Java 17
    - Docker & Docker Compose

2. **Docker Setup**
   ```yaml
   # services.yml
   name: active-mq-service
   services:
     postgresql:
       extends:
         file: ./postgres.yml
         service: postgresql
     activemq:
       extends:
         file: ./active-mq.yml
         service: activemq
   ```

3. **Start Docker Services**
   ```bash
   docker-compose -f services.yml up -d
   ```

4. **Configuration (application.yaml)**
   ```yaml
   spring:
     artemis:
       broker-url: tcp://localhost:61616
       username: admin
       password: admin
     datasource:
       url: jdbc:postgresql://localhost:5431/active-mq
       username: postgres
   ```

5. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints
- Queue Message: `GET /queue-publish-message?msg={message}`
- Topic Message: `GET /topic-publish-message?msg={message}`

## Message Flow
1. Messages published via REST API
2. Processed by Queue/Topic consumers
3. Failed messages retry with backoff
4. Exceeded retries go to DLQ
5. All actions tracked in database

## Docker Services
- **PostgreSQL**: Database for message auditing
- **ActiveMQ**: Message broker service

## Developer
Tajdin Gurdal (tajdingurdal@gmail.com)
