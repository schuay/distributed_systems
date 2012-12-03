package com.ds.event;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public abstract class Event implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String type;
    private final Date time;
    private final UUID id;

    protected Event(String type) {
        this.type = type;
        time = new Date();
        id = UUID.randomUUID();
    }

    public final String getType() {
        return type;
    }

    public final long getTimestamp() {
        return time.getTime();
    }

    public final Date getTime() {
        return time;
    }

    public final UUID getId() {
        return id;
    }
}
