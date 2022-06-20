package de.kosit.xmlmutate.runner;


import org.apache.logging.log4j.Level;

public enum LogLevel {
    /**
     * The log levels according to Log4j2
     */
    OFF (Level.OFF),
    FATAL (Level.FATAL),
    ERROR (Level.ERROR),
    WARN (Level.WARN),
    INFO (Level.INFO),
    DEBUG (Level.DEBUG),
    TRACE (Level.TRACE),
    ALL (Level.ALL);

    public final Level level;

    LogLevel(final Level level) {
        this.level = level;
    }

}