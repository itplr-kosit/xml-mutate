// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutator.Mutator;

/**
 * Mutates the original document.
 *
 * @author Andreas Penski
 */
public class MutateAction implements RunAction {
    @java.lang.SuppressWarnings("all")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MutateAction.class);

    @Override
    public void run(final Mutation mutation) {
        log.info("Running mutation {} on element {}", mutation.getMutator().getPreferredName(), mutation.getContext().getTarget().getNodeName());
        final Mutator mutator = mutation.getMutator();
        mutator.mutate(mutation.getContext(), mutation.getConfiguration());
        mutation.setState(State.MUTATED);
    }
}
