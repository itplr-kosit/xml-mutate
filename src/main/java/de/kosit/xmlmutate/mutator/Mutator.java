package de.kosit.xmlmutate.mutator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * package de.kosit.xmlmutate.mutator;Mutator
 *
 *
 */
public interface Mutator {

    public String getName();

    public MutatorConfig getConfig();

    public Node execute(Element context);

}