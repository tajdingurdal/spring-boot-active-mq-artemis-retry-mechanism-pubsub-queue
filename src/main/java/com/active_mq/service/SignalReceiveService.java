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

@Service
public class SignalReceiveService {

    protected final Logger log = LoggerFactory.getLogger(SignalReceiveService.class);

    private final List<BaseJMSConsumer> consumers;
    private final JmsTemplate jmsTemplate;

    public SignalReceiveService(List<BaseJMSConsumer> consumers, JmsTemplate jmsTemplate) {
        this.consumers = consumers;
        this.jmsTemplate = jmsTemplate;
    }

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

    public BaseMessage receiveMainMessage(String destination) {
        BaseMessage baseMessage = (BaseMessage) jmsTemplate.receiveAndConvert(destination);
        log.info("Receiving the main message.... {}", baseMessage);
        return baseMessage;
    }

    protected BaseJMSConsumer getConsumer(ConsumerType consumerType) {
        return consumers.stream()
                .filter(consumer -> consumer.consumerType().equals(consumerType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Consumer not found for type: " + consumerType));
    }
}
