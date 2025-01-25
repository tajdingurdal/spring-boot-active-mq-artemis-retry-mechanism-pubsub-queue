package com.active_mq.utils;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;


public class MessageUtils {

    private static Random random = new Random();

    public static String createUniqueMessageId() {
        String mark = Long.toHexString(random.nextLong());
        return String.format("%o-%s-%s", System.currentTimeMillis(), mark, UUID.randomUUID());
    }

    public static Instant defaultExpirationDate() {
        return Instant.now().plusSeconds(3600);
    }
}
