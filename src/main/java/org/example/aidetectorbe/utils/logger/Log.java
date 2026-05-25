package org.example.aidetectorbe.utils.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {

    private static final Logger logger = LogManager.getLogger("GlobalLogger");

    public static void info(String message) {
        logger.info(message);
    }

    public static void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void debug(String message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public static void fatal(String message) {
        logger.fatal(message);
    }

    public static void fatal(String message, Throwable throwable) {
        logger.fatal(message, throwable);
    }

    public static void verbose(String message) {
        logger.trace(message);
    }

    public static void verbose(String message, Throwable throwable) {
        logger.trace(message, throwable);
    }

    private Log() {
        // Private constructor to prevent instantiation
    }
}