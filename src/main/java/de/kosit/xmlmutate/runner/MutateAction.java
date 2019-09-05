package de.kosit.xmlmutate.runner;

import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutator.Mutator;

/**
 * Mutates the original document.
 *
 * @author Andreas Penski
 */
@Slf4j
public class MutateAction implements RunAction {

    @Override
    public void run(final Mutation mutation) {
        log.info(
                "Running mutation {} on element {}", mutation.getMutator().getPreferredName(),
                mutation.getContext().getTarget().getNodeName());
        final Mutator mutator = mutation.getMutator();
        mutator.mutate(mutation.getContext(), mutation.getConfiguration());
        mutation.setState(State.MUTATED);
    }
}
