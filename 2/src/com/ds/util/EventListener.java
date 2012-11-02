package com.ds.util;

import com.ds.event.Event;

/**
 * Listeners are notified of all auction related events.
 */
public interface EventListener {
    void onEvent(Event event);
}
