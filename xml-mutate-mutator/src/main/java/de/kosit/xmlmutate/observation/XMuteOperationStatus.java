package de.kosit.xmlmutate.observation;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete status implementation, suitable either for
 * instantiating or subclassing.
 */
public class XMuteOperationStatus implements OperationStatus {

    private List<OperationStatus> children = new ArrayList<>();
    /**
     * The severity. One of
     * <ul>
     * <li><code>ERROR</code></li>
     * <li><code>WARNING</code></li>
     * <li><code>INFO</code></li>
     * <li>or <code>OK</code> (0)</li>
     * </ul>
     */
    private int severity = OK;

    /**
     * Plug-in-specific status code.
     */
    private int code;

    /**
     * Message, localized to the current locale.
     */
    private String message = "";

    /**
     * Wrapped exception, or <code>null</code> if none.
     */
    private Throwable exception = new Throwable();

    public XMuteOperationStatus() {
        // sets severity ok and empty message
    }

    /**
     * Creates a new status object.
     *
     * @param severity the severity; one of <code>OK</code>,
     *                 <code>ERROR</code>, <code>INFO</code>, or
     *                 <code>WARNING</code>
     * @param message  a human-readable message, localized to the
     *                 current locale
     */
    public XMuteOperationStatus(int severity, String message) {
        setSeverity(severity);
        setMessage(message);
    }

    /**
     * Creates a new status object.
     *
     * @param severity  the severity; one of <code>OK</code>,
     *                  <code>ERROR</code>, <code>INFO</code>, or
     *                  <code>WARNING</code>
     * @param code      the plug-in-specific status code, or <code>OK</code>
     * @param message   a human-readable message, localized to the
     *                  current locale
     * @param exception a low-level exception, or <code>null</code> if not
     *                  applicable
     */
    public XMuteOperationStatus(int severity, int code, String message, Throwable exception) {
        this(severity, message);
        setCode(code);
        setException(exception);
    }

    /*
     * (Intentionally not javadoc'd)
     * Implements the corresponding method on <code>IStatus</code>.
     */
    @Override
    public int getCode() {
        return code;
    }

    /*
     * (Intentionally not javadoc'd)
     * Implements the corresponding method on <code>IStatus</code>.
     */
    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getSeverity() {
        return severity;
    }

    @Override
    public boolean isOK() {
        return severity == OK;
    }

    @Override
    public boolean isNotOK() {
        return severity > OK;
    }

    @Override
    public boolean matches(int severityMask) {
        return (severity & severityMask) != 0;
    }

    /**
     * Sets the status code.
     *
     * @param code specific status code, or <code>OK</code>
     */
    protected void setCode(int code) {
        this.code = code;
    }

    /**
     * Sets the exception.
     *
     * @param exception a low-level exception
     * 
     */
    protected void setException(Throwable exception) {
        this.exception = exception;
    }

    /**
     * Sets the message.
     *
     * @param message a human-readable message, localized to the
     *                current locale
     */
    protected void setMessage(String message) {
        if (message != null && !message.isBlank()) {
            this.message = message;
        }
    }

    /**
     * Sets the severity.
     *
     * @param severity the severity; one of <code>OK</code>,
     *                 <code>ERROR</code>, <code>INFO</code>, or
     *                 <code>WARNING</code>
     */
    protected void setSeverity(int severity) {
        if (severity == OK || severity == ERROR || severity == WARNING || severity == INFO) {
            this.severity = severity;
        } else {
            throw new IllegalArgumentException("Unknown severity for this status=" + severity);
        }

    }

    @Override
    public boolean hasMultiStatus() {
        return this.children.size() > 0;
    }

    @Override
    public void add(OperationStatus s) {
        this.children.add(s);
        if (s.getSeverity() > this.getSeverity()) {
            this.setSeverity(s.getSeverity());
            this.setMessage("There are issues");
        }
    }

    @Override
    public List<OperationStatus> getChildren() {
        return this.getChildren();
    }

    /**
     * Returns a string representation of the status, suitable
     * for debugging purposes only.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Status ");
        if (severity == OK) {
            buf.append("OK");
        } else if (severity == ERROR) {
            buf.append("ERROR");
        } else if (severity == WARNING) {
            buf.append("WARNING");
        } else if (severity == INFO) {
            buf.append("INFO");
        } else {
            buf.append("severity=");
            buf.append(severity);
        }

        buf.append(" code=");
        buf.append(code);
        buf.append(" ");
        buf.append(message);
        buf.append(" ");
        buf.append(exception);
        return buf.toString();
    }

}
