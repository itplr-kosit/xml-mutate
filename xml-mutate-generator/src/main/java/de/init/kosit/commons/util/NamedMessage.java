package de.init.kosit.commons.util;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Tagging interface for all messages that have a fixed ID and reference a specific text block.
 * 
 * @author André Babinsky
 * @author apenski
 */
public interface NamedMessage extends Serializable {

    /**
     * The class that contains all messages for this error bundle.
     *
     * @return the declaring class of the associated enum
     */
    Class<? extends NamedMessage> getDeclaringClass();

    /**
     * Resolves the ID of this message.
     *
     * @return the name of this message
     */
    default String getMessage(final Object... parameters) {
        return MessageFormat.format(getTemplate(), parameters);
    }

    /**
     * Returns the a template for the message.
     *
     * @return the template to use
     */
    default String getTemplate() {
        return name();
    }

    /**
     * Resolves the ID of this message.
     *
     * @return the name of this message
     */
    String name();

}
