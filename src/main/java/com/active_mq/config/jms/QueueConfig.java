package com.active_mq.config.jms;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.Session;
import java.util.Random;
import java.util.UUID;

/**
 * Configuration class for setting up JMS listener container factories.
 * It provides beans for creating container factories for different queue types.
 */
@Configuration
public class QueueConfig {

    private final ActiveMQConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;

    public QueueConfig(ActiveMQConnectionFactory connectionFactory,
                       MessageConverter messageConverter) {
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }

    /**
     * Creates and returns a JMS listener container factory for the default queue.
     * @return DefaultJmsListenerContainerFactory for the queue.
     */
    @Bean
    public DefaultJmsListenerContainerFactory containerFactory() {
        return createJmsListenerContainerFactory("queue");
    }

    /**
     * Creates and returns a JMS listener container factory for the dead-letter queue.
     * @return DefaultJmsListenerContainerFactory for the DLQ.
     */
    @Bean
    public DefaultJmsListenerContainerFactory dlqJmsListenerContainerFactory() {
        return createJmsListenerContainerFactory("queue-dlq");
    }

    /**
     * Creates and returns a JMS listener container factory for the expiry queue.
     * @return DefaultJmsListenerContainerFactory for the expiry queue.
     */
    @Bean
    public DefaultJmsListenerContainerFactory expiryJmsListenerContainerFactory() {
        return createJmsListenerContainerFactory("queue-expiry");
    }

    /**
     * Creates a JMS listener container factory with specific configuration for a given queue type.
     * @param type The type of the queue (e.g., default, DLQ, expiry).
     * @return Configured DefaultJmsListenerContainerFactory.
     */
    public DefaultJmsListenerContainerFactory createJmsListenerContainerFactory(String type) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setClientId(String.format("%s-%o-%o-%s", type, new Random().nextInt(), System.currentTimeMillis(), UUID.randomUUID()));
        factory.setAutoStartup(true);
        factory.setConcurrency("1");
        factory.setErrorHandler(t -> {
            System.err.println("Error in listener for queue: " + t.getMessage());
            t.printStackTrace();
        });
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return factory;
    }
}
