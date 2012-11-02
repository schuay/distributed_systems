package com.ds.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.ds.event.Event;

public class EventLogger implements EventListener {

    private final Logger logger = Logger.getLogger(EventLogger.class);

    public EventLogger() {
        BasicConfigurator.configure();
        logger.info(String.format("%s initialized", EventLogger.class.getName()));
    }

    @Override
    public void onEvent(Event event) {
        logger.info(event.toString());
    }

}
