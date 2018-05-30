package de.kosit.xmlmutate.mutator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ChangeTextMutator
 */
public class ChangeTextMutator extends AbstractMutator {

private static String MUTATOR_NAME = "ch-txt";

    public ChangeTextMutator(MutatorConfig config) {
        this.addConfig(config);
    }

    @Override
    public String getName() {
        return MUTATOR_NAME;
    }

    @Override
    public Node execute(Element context) {
        return null;
    }

}