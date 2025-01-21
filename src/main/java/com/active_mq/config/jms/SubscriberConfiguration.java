package com.active_mq.config.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;

@Configuration
public class SubscriberConfiguration {

    private final ActiveMQConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;

    public SubscriberConfiguration(ActiveMQConnectionFactory connectionFactory, MessageConverter messageConverter) {
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsSubscriberListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setClientId("unique-subscriber-listener-client-id");
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
