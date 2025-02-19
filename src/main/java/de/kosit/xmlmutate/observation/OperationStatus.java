package de.kosit.xmlmutate.observation;

import java.util.List;

/**
 * A status object represents the outcome of an operation on a Domain Entity.
 * Status objects are also returned by methods needing
 * to provide details of failures (e.g., validation methods).
 * <p>
 * A status carries the following information:
 * <ul>
 * <li>Entity identifier (required)</li>
 * <li>severity (required)</li>
 * <li>message (required) - localized to current locale</li>
 * <li>status code (optional)</li>
 * <li>exception (optional) - for problems stemming from a failure at
 * a lower level</li>
 * </ul>
 * Some status objects, known as multi-statuses, have other status objects
 * as children.
 * </p>
 * <p>
 * The class <code>Status</code> is the standard public implementation
 * of status objects; the subclass <code>MultiStatus</code> is the
 * implements multi-status objects.
 * </p>
 * 
 * @see XMuteOperationStatus
 */
public interface OperationStatus {

    /**
     * Status severity constant (value 0) indicating this status represents the
     * nominal case.
     * This constant is also used as the status code representing the nominal case.
     * 
     * @see #getSeverity
     * @see #isOK
     */
    public static final int OK = 0;

    /**
     * Status type severity (bit mask, value 1) indicating this status is
     * informational only.
     * 
     * @see #getSeverity
     * @see #matches
     */
    public static final int INFO = 1;

    /**
     * Status type severity (bit mask, value 2) indicating this status represents a
     * warning.
     * 
     * @see #getSeverity
     * @see #matches
     */
    public static final int WARNING = 2;

    /**
     * Status type severity (bit mask, value 4) indicating this status represents an
     * error.
     * 
     * @see #getSeverity
     * @see #matches
     */
    public static final int ERROR = 4;



    /**
     * Returns a list of status object immediately contained in this
     * multi-status, or an empty list if this is not a multi-status.
     *
     * @return an array of status objects
     * @see #isMultiStatus
     */
    // public Status[] getChildren();
    /**
     * Returns a specific status code describing the outcome.
     *
     * @return specific status code
     */
    public int getCode();

    /**
     * Returns the relevant low-level exception, or <code>null</code> if none.
     * For example, when an operation fails because of a network communications
     * failure, this might return the <code>java.io.IOException</code>
     * describing the exact nature of that failure.
     *
     * @return the relevant low-level exception, or <code>null</code> if none
     */
    public Throwable getException();

    /**
     * Returns the message describing the outcome.
     * The message is localized to the current locale.
     *
     * @return a localized message
     */
    public String getMessage();

    /**
     * Returns the severity. The severities are as follows (in
     * descending order):
     * <ul>
     * <li><code>ERROR</code> - a serious error (most severe)</li>
     * <li><code>WARNING</code> - a warning (less severe)</li>
     * <li><code>INFO</code> - an informational ("fyi") message (least severe)</li>
     * <li><code>OK</code> - everything is just fine</li>
     * </ul>
     * <p>
     * The severity of a multi-status is defined to be the maximum
     * severity of any of its children, or <code>OK</code> if it has
     * no children.
     * </p>
     *
     * @return the severity: one of <code>OK</code>,
     *         <code>ERROR</code>, <code>INFO</code>, or <code>WARNING</code>
     * @see #matches
     */
    public int getSeverity();

    /**
     * Returns a hint on how the current Status may be changed.
     * 
     * @return the hint on how this status can be changes
     */
    default String getFixHint() {
        return "unknown";
    }

    /**
     * Returns the location on where the status was generated.
     * 
     * @return A Symbolic representation of the location e.g. "Line 1, Offset 10" or
     *         similar
     */
    default String getLocation() {
        return "unknown";
    }

    /**
     * Returns whether this status is a multi-status.
     * A multi-status describes the outcome of an operation
     * involving multiple operands.
     * <p>
     * The severity of a multi-status is derived from the severities
     * of its children; a multi-status with no children is
     * <code>OK</code> by definition.
     * A multi-status carries a plug-in identifier, a status code,
     * a message, and an optional exception. Clients may treat
     * multi-status objects in a multi-status unaware way.
     * </p>
     *
     * @return <code>true</code> for a multi-status,
     *         <code>false</code> otherwise
     * @see #getChildren
     */
    public boolean hasMultiStatus();

    public void add(OperationStatus s);

    public List<OperationStatus> getChildren();

    /**
     * Returns whether this status indicates everything is okay
     * (neither info, warning, nor error).
     *
     * @return <code>true</code> if this status has severity
     *         <code>OK</code>, and <code>false</code> otherwise
     */
    public boolean isOK();

    public boolean isNotOK();

    /**
     * Returns whether the severity of this status matches the given
     * specification.
     *
     * @param severityMask a mask formed by bitwise or'ing severity mask
     *                     constants (<code>ERROR</code>, <code>WARNING</code>,
     *                     <code>INFO</code>)
     * @return <code>true</code> if there is at least one match,
     *         <code>false</code> if there are no matches
     * @see #getSeverity
     * @see #ERROR
     * @see #WARNING
     * @see #INFO
     */
    public boolean matches(int severityMask);

}
