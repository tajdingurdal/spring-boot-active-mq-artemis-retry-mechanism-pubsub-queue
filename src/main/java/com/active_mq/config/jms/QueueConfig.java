package com.active_mq.config.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;

import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;

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
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setClientId(String.format("queue-%o-%o-%s", new Random().nextInt(), System.currentTimeMillis(), UUID.randomUUID()));
        factory.setAutoStartup(true);
        factory.setConcurrency("1-10");
        factory.setErrorHandler(t -> {
            System.err.println("Error in listener for queue: " + t.getMessage());
            t.printStackTrace();
        });
        return factory;
    }
}
