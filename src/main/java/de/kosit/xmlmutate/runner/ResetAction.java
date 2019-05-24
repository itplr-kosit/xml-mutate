package de.kosit.xmlmutate.runner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * Diese Aktion stellt die ursprünglichen Zustand des {@link Document} wieder her.
 * 
 * @author Andreas Penski
 */
public class ResetAction implements RunAction {

    @Override
    public void run(final Mutation mutation) {
        final MutationContext context = mutation.getContext();
        final Element parent = context.getParentElement();
        parent.replaceChild(context.getOriginalFragment(), context.getTarget());
    }
}
