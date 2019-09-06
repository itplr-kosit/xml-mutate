package de.kosit.xmlmutate.runner;

import java.util.stream.IntStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * Diese Aktion stellt den ursprünglichen Zustand des {@link Document}s nach der
 * Mutation wieder her.
 *
 * @author Andreas Penski
 */
class ResetAction implements RunAction {

    @Override
    public void run(final Mutation mutation) {
        final MutationContext context = mutation.getContext();
        final Node parent = context.getPi().getParentNode();
        if (parent == null || context.getTarget() == null) {
            throw new MutationException(ErrorCode.RESET_NULL);
        }
        if (!context.getTarget().getOwnerDocument().isSameNode(context.getPi().getOwnerDocument())) {
            throw new MutationException(ErrorCode.WRONG_OWNER);
        }
        if (!parent.isSameNode(context.getDocument())) {
            if (!context.getParentElement().isSameNode(context.getTarget().getParentNode())) {
                throw new MutationException(ErrorCode.WRONG_PARENT);
            }
            parent.replaceChild(context.getOriginalFragment(), context.getTarget());
        } else {
            resetRootNode(context);
        }
    }

    private void resetRootNode(final MutationContext context) {
        // root node requires different handling
        final Element root = context.getDocument().getDocumentElement();

        // remove all attributes from root
        
        while (root.getAttributes().getLength() > 0) {
            final Node att = root.getAttributes().item(0);
            root.getAttributes().removeNamedItem(att.getNodeName());
        }

        // remove all child nodes from root
        while (root.getChildNodes().getLength() > 0) {
            final Node child = root.getChildNodes().item(0);
            root.removeChild(child);
        }

        // start reconstruct element content
        final Node origRoot = context.getOriginalFragment().getChildNodes().item(0);
        final NodeList children = origRoot.getChildNodes();
        final NamedNodeMap attributes = origRoot.getAttributes();

        // set all attributes again
        IntStream.range(0, attributes.getLength()).mapToObj(attributes::item)
                .forEach(e -> root.setAttribute(e.getNodeName(), e.getNodeValue()));

        // move child nodes from fragment to root
        while (children.getLength() > 0) {
            root.appendChild(children.item(0));
        }
    }

    @Override
    public void run(RunnerDocumentContext context) {

    }

}
