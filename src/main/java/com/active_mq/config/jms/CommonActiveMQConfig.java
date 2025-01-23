package com.active_mq.config.jms;

import com.active_mq.config.JMSProperties;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.List;

@Configuration
@EnableJms
public class CommonActiveMQConfig {

    private final Jackson2ObjectMapperBuilder mapperBuilder;
    private final JMSProperties jmsProperties;

    public CommonActiveMQConfig(Jackson2ObjectMapperBuilder mapperBuilder, JMSProperties jmsProperties) {
        this.mapperBuilder = mapperBuilder;
        this.jmsProperties = jmsProperties;
    }

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(jmsProperties.getBrokerUrl());
        factory.setUserName(jmsProperties.getUsername());
        factory.setPassword(jmsProperties.getPassword());
        factory.setTrustAllPackages(false);
        factory.setTrustedPackages(List.of("com.active_mq.model"));


        ActiveMQPrefetchPolicy activeMQPrefetchPolicy = new ActiveMQPrefetchPolicy();
        activeMQPrefetchPolicy.setQueuePrefetch(100);
        factory.setPrefetchPolicy(activeMQPrefetchPolicy);

        return factory;
    }

//    @Bean
//    public PooledConnectionFactory pooledConnectionFactory() {
//        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
//        pooledConnectionFactory.setConnectionFactory(connectionFactory());
//        pooledConnectionFactory.setMaxConnections(10);
//        return pooledConnectionFactory;
//    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory());
        template.setMessageConverter(jacksonJmsMessageConverter());
        template.setTimeToLive(jmsProperties.getMessageExpireTime());
        return template;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.BYTES);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(mapperBuilder.build());
        return converter;
    }
}
