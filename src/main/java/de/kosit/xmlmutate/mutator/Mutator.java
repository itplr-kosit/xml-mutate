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

    public void addConfig(MutatorConfig config);

    public Node execute(Element context);

}