package com.active_mq.config.jms;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.Session;
import java.util.Random;
import java.util.UUID;

@Configuration
public class QueueConfig {

    private final ActiveMQConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;

    public QueueConfig(ActiveMQConnectionFactory connectionFactory,
                       MessageConverter messageConverter) {
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }

    @Bean
    public DefaultJmsListenerContainerFactory containerFactory() {
        return createJmsListenerContainerFactory("queue");
    }

    @Bean
    public DefaultJmsListenerContainerFactory dlqJmsListenerContainerFactory() {
        return createJmsListenerContainerFactory("queue-dlq");
    }

    @Bean
    public DefaultJmsListenerContainerFactory expiryJmsListenerContainerFactory() {
        return createJmsListenerContainerFactory("queue-expiry");
    }

    public DefaultJmsListenerContainerFactory createJmsListenerContainerFactory(String type) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setClientId(String.format("%s-%o-%o-%s", type, new Random().nextInt(), System.currentTimeMillis(), UUID.randomUUID()));
        factory.setAutoStartup(true);
        factory.setConcurrency("1-1");
        factory.setErrorHandler(t -> {
            System.err.println("Error in listener for queue: " + t.getMessage());
            t.printStackTrace();
        });
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return factory;
    }
}
