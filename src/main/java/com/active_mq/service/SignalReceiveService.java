package com.active_mq.service;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.dto.SignalMessage;
import com.active_mq.model.enums.ConsumerType;
import com.active_mq.service.jms.consumer.abstrct.BaseJMSConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Service class responsible for receiving and processing signal messages using JMS.
 */
@Service
public class SignalReceiveService {

    protected final Logger log = LoggerFactory.getLogger(SignalReceiveService.class);

    private final List<BaseJMSConsumer> consumers;
    private final JmsTemplate jmsTemplate;

    public SignalReceiveService(List<BaseJMSConsumer> consumers, JmsTemplate jmsTemplate) {
        this.consumers = consumers;
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Listens to the signal message queue, processes the signal, and delegates to the appropriate consumer.
     *
     * @param signal SignalMessage received from the queue.
     * @param <T>    Type of BaseMessage.
     * @throws MessageProcessingException if processing the message fails.
     */
    @JmsListener(destination = "${spring.artemis.destination.start-signal-queue}",
            containerFactory = "containerFactory")
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public <T extends BaseMessage> void signal(final SignalMessage signal) throws MessageProcessingException {
        BaseJMSConsumer consumer = getConsumer(signal.getConsumerType());
        BaseMessage mainMessage = receiveMainMessage(signal.getDestination());
        assert mainMessage != null;
        consumer.processMainMessage(mainMessage);
    }

    /**
     * Receives the main message from the specified destination queue.
     *
     * @param destination Queue name to receive the message from.
     * @return The received BaseMessage or null if no message is available.
     */
    public BaseMessage receiveMainMessage(String destination) {
        BaseMessage baseMessage = (BaseMessage) jmsTemplate.receiveAndConvert(destination);
        log.info("Receiving the main message.... {}", baseMessage);
        return baseMessage;
    }

    /**
     * Retrieves the appropriate consumer for the given ConsumerType.
     *
     * @param consumerType The type of consumer to retrieve.
     * @return The matching BaseJMSConsumer instance.
     * @throws RuntimeException if no consumer is found for the given type.
     */
    public BaseJMSConsumer getConsumer(ConsumerType consumerType) {
        return consumers.stream()
                .filter(consumer -> consumer.consumerType().equals(consumerType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Consumer not found for type: " + consumerType));
    }
}
