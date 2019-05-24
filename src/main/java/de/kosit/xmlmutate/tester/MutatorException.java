package de.kosit.xmlmutate.tester;

/**
 * @author Andreas Penski
 */
public class MutatorException extends Throwable {

    public MutatorException(final String message) {
        super(message);
    }

    public MutatorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MutatorException(final Throwable cause) {
        super(cause);
    }

    public MutatorException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MutatorException() {
    }
}
