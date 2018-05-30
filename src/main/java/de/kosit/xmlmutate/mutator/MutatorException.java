package de.kosit.xmlmutate.mutator;

/**
 * MutatorException
 *
 * @author Renzo Kottmann
 */
public class MutatorException extends RuntimeException {

    private static final long serialVersionUID = 2875909366029688823L;

    public MutatorException(String message) {
        super(message);
    }

    public MutatorException(String message, Throwable cause) {
        super(message, cause);
    }
}