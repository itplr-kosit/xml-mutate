package de.kosit.xmlmutate;

/**
 * MutatorException
 *
 * @author Renzo Kottmann
 */
public class XMLMutateException extends RuntimeException {

    private static final long serialVersionUID = -4718946199360474607L;
    private Status status = Status.OK;

    // public XMLMutateException(String message, int statusCode) {
    //     super(message);
    //     this.statusCode = statusCode;
    // }

    // public XMLMutateException(String message, int statusCode, Throwable cause) {
    //     super(message, cause);
    //     this.statusCode = statusCode;
    // }

    public XMLMutateException(String message, Status status) {
        super(message);
        this.status = status;
    }

    public XMLMutateException(String message, Status status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public static enum Status {
        OK(0),
        FILE_ERROR(10),
        PARSER_ERROR(20);

        private int value;

        private Status(int value) {
            this.value = value;
        }

        public int code() {
            return value;
        }
    }
}