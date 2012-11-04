package com.ds.event;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Event implements Serializable {

    private static final long serialVersionUID = 1L;
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
