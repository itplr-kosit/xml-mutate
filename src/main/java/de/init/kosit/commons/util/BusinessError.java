package de.init.kosit.commons.util;

/**
 * Interface for all business-logic-relevant obvious named errors.
 *
 * @author Sascha Sengespeick
 * @author Tobias Liefke
 */
public final class BusinessError extends Message {

    private static final long serialVersionUID = -4414586233882466999L;

    private Exception rootCause;

    /**
     * Creates a new instance of {@link BusinessError}.
     *
     * @param errorId NamedMessage
     * @param parameters parameters
     */
    public BusinessError(final NamedMessage errorId, final Object... parameters) {
        super(errorId, parameters);
    }

}
