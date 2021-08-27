// Generated by delombok at Fri Aug 13 16:11:22 CEST 2021
package de.init.kosit.commons.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Combines a {@link NamedMessage} and its replaceable text parameters.
 *
 * @author Andreas Penski
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -4050881107799848050L;
    private final NamedMessage messageId;
    private final transient Object[] params;

    /**
     * Creates an instance of a Message.
     *
     * @param messageId the ID of the NamedMessage
     * @param parameters the parameters for the later message
     */
    public Message(final NamedMessage messageId, final Object... parameters) {
        this.messageId = messageId;
        this.params = parameters;
    }

    /**
     * Returns the message associated with this {{@link #messageId}}. Message construction depends on the actual
     * implementation of {@link Message#getMessage()}
     *
     * @return an error message
     */
    public String getMessage() {
        return messageId.getMessage(getParams());
    }

    /**
     * Returns the {@link #messageId} and {@link #params}.
     */
    @Override
    public String toString() {
        String result = this.messageId.name();
        if (this.params != null && this.params.length > 0) {
            result += '(' + Arrays.stream(params).map(Object::toString).collect(Collectors.joining()) + ')';
        }
        return result;
    }

    @java.lang.SuppressWarnings("all")
    public NamedMessage getMessageId() {
        return this.messageId;
    }

    @java.lang.SuppressWarnings("all")
    public Object[] getParams() {
        return this.params;
    }
}