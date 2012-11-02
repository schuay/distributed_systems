package com.ds.event;

import java.util.Date;
import java.util.UUID;

public class Event {

    private final String type;
    private final long timestamp;
    private final UUID id;

    protected Event(String type) {
        this.type = type;
        timestamp = new Date().getTime();
        id = UUID.randomUUID();
    }

    public final String getType() {
        return type;
    }

    public final long getTimestamp() {
        return timestamp;
    }

    public final UUID getId() {
        return id;
    }
}
