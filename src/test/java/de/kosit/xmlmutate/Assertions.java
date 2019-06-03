package de.kosit.xmlmutate;

import org.w3c.dom.Node;

/**
 * Nützliche Erweiterungen der AssertJ assertions.
 *
 * @author Andreas Penski
 */
public class Assertions {

    /**
     * Assertion für {@link Node}, inklusive {@link org.w3c.dom.Element}, {@link org.w3c.dom.Attr} und
     * {@link org.w3c.dom.Document}.
     *
     * @param actual der Knoten der getestet werden soll
     * @return {@link NodeAssert}
     */
    public static NodeAssert assertThat(final Node actual) {
        return new NodeAssert(actual);
    }
}
