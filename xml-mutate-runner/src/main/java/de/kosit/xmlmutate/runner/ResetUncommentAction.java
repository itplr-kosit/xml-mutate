package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutator.AlternativeMutator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This action recovers the original state of {@link Document} after the {@code alternative} mutation
 */
class ResetUncommentAction implements RunAction {

    @Override
    public void run(final Mutation mutation) {
        if (mutation.getMutator() instanceof AlternativeMutator) {
            final Node parent = mutation.getContext().getPi().getParentNode();
            mutation.getContext().getMutatedTargets().forEach(parent::removeChild);
        }
    }
}
