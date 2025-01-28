package com.active_mq.config.jms;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;

import java.util.Random;
import java.util.UUID;

/**
 * Configuration class for setting up a JMS subscriber listener container factory.
 * It provides a bean for creating a container factory for a topic subscriber.
 */
@Configuration
public class SubscriberConfiguration {

    private final ActiveMQConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;

    public SubscriberConfiguration(ActiveMQConnectionFactory connectionFactory, MessageConverter messageConverter) {
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }

    /**
     * Creates and returns a JMS listener container factory for a topic subscriber.
     * The factory is configured with durable subscription and pub-sub domain.
     * @return Configured DefaultJmsListenerContainerFactory for the topic subscriber.
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsSubscriberListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setClientId(String.format("topic-subscriber-%o-%o-%s", new Random().nextInt(), System.currentTimeMillis(), UUID.randomUUID()));
        factory.setAutoStartup(true);
        factory.setPubSubDomain(true);
        factory.setSubscriptionDurable(true);
        factory.setErrorHandler(t -> {
            System.err.println("Error in listener for subscriber: " + t.getMessage());
            t.printStackTrace();
        });
        return factory;
    }
}
