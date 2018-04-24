package de.kosit.xmlmutate.mutator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Mutator
 * @author Renzo Kottmann
 */
public class EmptyMutator implements Mutator {

    private final static Logger log = LogManager.getLogger(Mutator.class);
    private final static String MUTATOR_NAME = "empty";
    MutatorConfig config = null;

    EmptyMutator(MutatorConfig config) {
        this.config = config;
    }

    public String getName() {
        return EmptyMutator.MUTATOR_NAME;
    }

    @Override
    public Node execute(Element context) {

        log.debug("Element to make empty" + context);
        Document doc = context.getOwnerDocument();
        Node child = context.getFirstChild();
        log.debug("First child of context is=" + child);
        context.removeChild(child);
        //doc.removeChild(context.getFirstChild());
        return context;
    }
}