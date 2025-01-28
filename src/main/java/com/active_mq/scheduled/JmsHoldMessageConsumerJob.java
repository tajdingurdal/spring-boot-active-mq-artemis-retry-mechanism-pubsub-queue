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

/**
 * A job that consumes messages from a JMS queue, processes them if not expired.
 * This class is scheduled to run periodically and uses JMSTemplate for message receiving.
 */
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

    /**
     * Scheduled method that receives and processes messages from the JMS queue.
     * The method runs periodically with a fixed rate and checks if messages are expired before processing.
     */
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
