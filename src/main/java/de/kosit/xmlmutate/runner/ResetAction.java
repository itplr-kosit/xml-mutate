package de.kosit.xmlmutate.runner;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * Diese Aktion stellt den ursprünglichen Zustand des {@link Document} wieder her.
 * 
 * @author Andreas Penski
 */
class ResetAction implements RunAction {

    @Override
    public void run(final Mutation mutation) throws MutationException {
        final MutationContext context = mutation.getContext();
        final Node parent = context.getPi().getParentNode();
        if (parent == null || context.getTarget() == null) {
            throw new MutationException(ErrorCode.RESET_NULL);
        }
        if (!context.getTarget().getOwnerDocument().isSameNode(context.getPi().getOwnerDocument())) {
            throw new MutationException(ErrorCode.WRONG_OWNER);
        }
        if (!context.getParentElement().isSameNode(context.getTarget().getParentNode())) {
            throw new MutationException(ErrorCode.WRONG_PARENT);
        }
        parent.replaceChild(context.getOriginalFragment(), context.getTarget());
    }
}
