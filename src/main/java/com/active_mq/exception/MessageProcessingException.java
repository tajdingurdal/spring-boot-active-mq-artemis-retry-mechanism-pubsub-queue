package com.active_mq.exception;

public class MessageProcessingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MessageProcessingException(String message) {
        super(message);
    }

    public MessageProcessingException(String message, Exception e) {
        super(message, e);
    }
}
