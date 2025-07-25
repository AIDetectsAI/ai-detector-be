package org.example.aidetectorbe.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {

    private static final Logger logger = LogManager.getLogger("GlobalLogger");

    public static void info(String message) {logger.info(message);}

    public static void error(String message) {logger.error(message);}

    public static void debug(String message) {logger.debug(message);}

    public static void warn(String message) {logger.warn(message);}

    public static void fatal(String message) {logger.fatal(message);}

    public static void verbose(String message) {logger.trace(message);}

    private Log() {
        // Private constructor to prevent instantiation
    }
}