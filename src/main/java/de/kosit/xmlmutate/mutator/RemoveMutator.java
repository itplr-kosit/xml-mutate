package de.kosit.xmlmutate.mutator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Mutator
 *
 * @author Renzo Kottmann
 */
public class RemoveMutator implements Mutator {

    private final static Logger log = LogManager.getLogger(Mutator.class);
    private final static String MUTATOR_NAME = "remove";
    MutatorConfig config = null;

    RemoveMutator(MutatorConfig config) {
        this.addConfig(config);
    }

    private void addConfig(MutatorConfig config) {
        this.config = config;
    }

    public String getName() {
        return RemoveMutator.MUTATOR_NAME;
    }

    @Override
    public Node execute(Element context) {

        log.debug("Element to remove" + context);
        Document doc = context.getOwnerDocument();
        Comment remark = doc.createComment("Removed node " + context.getTagName());
        Node parent = context.getParentNode();
        log.debug("Parent of context is=" + parent);
        Node replaced = parent.replaceChild(remark, context);
        log.debug("replaced node=" + replaced);
        // parent.appendChild(remark);
        // doc.removeChild(context.getFirstChild());
        return remark;
    }

    public Node executeNewNode(Element context) {

        log.debug("Element to remove" + context);
        Document doc = context.getOwnerDocument();
        Comment remark = doc.createComment("Removed node " + context.getTagName());
        Node parent = context.getParentNode();
        log.debug("Parent of context is=" + parent);
        Node replaced = parent.replaceChild(remark, context);
        log.debug("replaced node=" + replaced);
        // parent.appendChild(remark);
        // doc.removeChild(context.getFirstChild());
        return remark;
    }

    @Override
    public MutatorConfig getConfig() {
        return this.config;
    }
}