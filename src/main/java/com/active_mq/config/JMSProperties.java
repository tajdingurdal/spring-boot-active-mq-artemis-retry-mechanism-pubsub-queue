package com.active_mq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "spring.activemq")
@Validated
public class JMSProperties {

    private String username;
    private String password;
    private String brokerUrl;
    private Integer messageExpireTime;
    private Redelivery redelivery;
    private Destination destination;

    public JMSProperties() {
    }

    public JMSProperties(String username, String password, String brokerUrl, Integer messageExpireTime, Redelivery redelivery, Destination destination) {
        this.username = username;
        this.password = password;
        this.brokerUrl = brokerUrl;
        this.messageExpireTime = messageExpireTime;
        this.redelivery = redelivery;
        this.destination = destination;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public Integer getMessageExpireTime() {
        return messageExpireTime;
    }

    public void setMessageExpireTime(Integer messageExpireTime) {
        this.messageExpireTime = messageExpireTime;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Redelivery getRedelivery() {
        return redelivery;
    }

    public void setRedelivery(Redelivery redelivery) {
        this.redelivery = redelivery;
    }

    public record Redelivery(int maxAttempts, int initialInterval, int multiplier, int maxInterval) {

    }

    public record Destination(String expiryQueue,
                              String deadLetterQueue,
                              String groupQueue,
                              String messageQueue,
                              String messageTopic) {

    }
}

