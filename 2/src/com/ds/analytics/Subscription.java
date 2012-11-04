package com.ds.analytics;

import java.util.UUID;
import java.util.regex.Pattern;

import com.ds.interfaces.EventProcessor;

public class Subscription {

    private final UUID uuid;
    private final Pattern pattern;
    private final EventProcessor callback;

    public Subscription(String regex, EventProcessor callback) {
        this.uuid = UUID.randomUUID();
        this.pattern = Pattern.compile(regex);
        this.callback = callback;
    }

    public UUID getUuid() {
        return uuid;
    }
}
