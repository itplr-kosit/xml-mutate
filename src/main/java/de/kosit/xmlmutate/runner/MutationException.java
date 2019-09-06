package de.kosit.xmlmutate.runner;

/**
 * @author Renzo Kottmann
 * @author Andreas Penski
 */
public class MutationException extends RuntimeException {

    private static final long serialVersionUID = -8460356990632230194L;
    private final ErrorCode code;

    public MutationException(ErrorCode code) {
        super(code.message());
        this.code = code;
    }

    public MutationException(String message) {
        super(ErrorCode.GENERAL_ERROR.message(message));
        this.code = ErrorCode.GENERAL_ERROR;
    }

    public MutationException(ErrorCode code, String message) {
        super(code.message(message));
        this.code = code;
    }

    public MutationException(ErrorCode code, String message, String messageDetail) {
        super(code.message(String.format(message, messageDetail)));
        this.code = code;
    }

    public MutationException(ErrorCode code, String messageDetail, Throwable cause) {
        super(code.message(messageDetail), cause);
        this.code = code;
    }

    //

    public MutationException(ErrorCode code, String messageDetail, final Exception e) {
        super(code.message(messageDetail), e);
        this.code = code;
    }

    public ErrorCode getCode() {
        return this.code;
    }
}
