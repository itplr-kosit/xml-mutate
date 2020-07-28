package de.kosit.xmlmutate.assertions;

import org.assertj.core.api.AbstractAssert;
import org.w3c.dom.*;

/**
 * Assertion for {@link Node}, including {@link Element}, {@link Attr} and {@link Document}.
 *
 * @author Andreas Penski
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class NodeListAssert extends AbstractAssert<NodeListAssert, NodeList> {

    /**
     * Constructor.
     *
     * @param node the node that should be tested
     */
    NodeListAssert(final NodeList node) {
        super(node, NodeListAssert.class);
    }

    public NodeListAssert hasSize(final int count) {
        if (this.actual.getLength() != count) {
            failWithMessage("Expected nodeliste size {0}. Actual is {1}", count, this.actual.getLength());
        }
        return this;
    }

}
