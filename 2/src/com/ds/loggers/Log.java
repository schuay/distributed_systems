package com.ds.loggers;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * Static logger class intended for general String logging.
 */
public class Log {

    private static final Logger logger = Logger.getLogger(Log.class);

    static {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        logger.info(String.format("%s initialized", EventLogger.class.getName()));
    }

    public static void d(String format, Object... args) {
        logger.debug(String.format(format, args));
    }

    public static void i(String format, Object... args) {
        logger.info(String.format(format, args));
    }

    public static void w(String format, Object... args) {
        logger.warn(String.format(format, args));
    }

    public static void e(String format, Object... args) {
        logger.error(String.format(format, args));
    }

    /* Purely static. */
    private Log() { }
}
