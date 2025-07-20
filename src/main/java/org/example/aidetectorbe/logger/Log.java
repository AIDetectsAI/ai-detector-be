package org.example.aidetectorbe.logger;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    @Getter
    private static final Logger logger = LogManager.getLogger("GlobalLogger");

    public static void info(String message) {
        logger.info(message);
    }

    public static void error(String message) {
        logger.error(message);
    }
}