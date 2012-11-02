package com.ds.interfaces;

import com.ds.event.Event;

/** An object which can receive and process events.
 * For example, the billing server, analytics server, and
 * management client should all implement this interface.
 */
public interface EventProcessor {

    void processEvent(Event event);

}
