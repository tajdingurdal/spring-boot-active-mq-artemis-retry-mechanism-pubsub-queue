package com.active_mq.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RedeliveryCountManager {

    private final Map<String, AtomicInteger> retryCountMap = new ConcurrentHashMap<>();

    /**
     * Retrieves the map containing all retry counts.
     *
     * @return the retry count map
     */
    public Map<String, AtomicInteger> getRetryCountMap() {
        return retryCountMap;
    }

    /**
     * Removes the retry count for a specific message ID.
     *
     * @param messageId the ID of the message to remove
     */
    public void removeRedeliveryCountFromMap(String messageId) {
        retryCountMap.remove(messageId);
    }

    /**
     * Retrieves the retry count for a specific message ID.
     * If no count exists, initializes it to 0.
     *
     * @param messageId the ID of the message
     * @return the retry count as an AtomicInteger
     */
    public AtomicInteger getRedeliveryCountFromMap(String messageId) {
        return retryCountMap.computeIfAbsent(messageId, k -> new AtomicInteger(1));
    }

    /**
     * Retrieves the retry count for a specific message ID as int.
     *
     * @param messageId the ID of the message
     * @return the retry count as an AtomicInteger
     */
    public int getRedeliveryCountAsInt(String messageId) {
        return getRedeliveryCountFromMap(messageId).get();
    }

    /**
     * Increments the retry count for a specific message ID.
     *
     * @param messageId the ID of the message
     */
    public void incrementRedeliveryCount(String messageId) {
        getRedeliveryCountFromMap(messageId).incrementAndGet();
    }

    /**
     * Resets the retry count for a specific message ID.
     *
     * @param messageId the ID of the message
     */
    public void resetRedeliveryCount(String messageId) {
        getRedeliveryCountFromMap(messageId).set(0);
    }

    /**
     * Remove the retry count for a specific message ID.
     *
     * @param messageId the ID of the message
     */
    public void remove(String messageId) {
        getRetryCountMap().remove(messageId);
    }
}
