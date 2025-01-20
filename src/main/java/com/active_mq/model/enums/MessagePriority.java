package com.active_mq.model.enums;

public enum MessagePriority {

    DEFAULT(4),
    HIGHEST(5),
    MEDIUM(3),
    LOW(2),
    LOWEST(1);

    private final int level;

    MessagePriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
