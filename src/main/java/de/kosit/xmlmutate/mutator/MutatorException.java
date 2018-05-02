package de.kosit.xmlmutate.mutator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MutatorException
 * @author Renzo Kottmann
 */
public class MutatorException extends RuntimeException {

    private final static Logger log = LogManager.getLogger(MutatorException.class);

    public MutatorException(String message) {
        super(message);
    }

    public MutatorException(String message, Throwable cause) {
        super(message, cause);
    }
}