package com.active_mq.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Random;


public class MessageUtils {

    private static Random random = new Random();

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public static String createMessageId(Date date) {
        String format = dateFormat.format(date);
        String mark = Long.toHexString(random.nextLong());
        return format.concat(mark);
    }

    public static Instant defaultExpirationDate(){
        return Instant.now().plusSeconds(3600);
    }
}
