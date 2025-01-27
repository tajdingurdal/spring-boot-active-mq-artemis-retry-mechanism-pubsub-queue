package com.active_mq.scheduled;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.service.jms.consumer.JMSQueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@EnableScheduling
public class JmsHoldMessageConsumerJob {

    Logger log = LoggerFactory.getLogger(JmsHoldMessageConsumerJob.class);

    private final JmsTemplate jmsTemplate;
    private final JMSProperties jmsProperties;
    private final JMSQueueConsumer jmsQueueConsumer;

    public JmsHoldMessageConsumerJob(JmsTemplate jmsTemplate, JMSProperties jmsProperties, JMSQueueConsumer jmsQueueConsumer) {
        this.jmsTemplate = jmsTemplate;
        this.jmsProperties = jmsProperties;
        this.jmsQueueConsumer = jmsQueueConsumer;
    }

    @Scheduled(initialDelay = 0, fixedRate = 3600000)
    public void receiveAllMessages() {
        log.info("Start receive all messages");
        boolean hasMore = true;

        while (hasMore) {
            try {
                BaseMessage baseMessage = (BaseMessage) jmsTemplate.receiveAndConvert(jmsProperties.getDestination().messageQueue());
                if (baseMessage != null) {
                    if (baseMessage.getExpirationDate() != null && Instant.now().isAfter(baseMessage.getExpirationDate())) {
                        jmsQueueConsumer.processMainMessage(baseMessage);
                    }
                } else {
                    hasMore = false;
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
