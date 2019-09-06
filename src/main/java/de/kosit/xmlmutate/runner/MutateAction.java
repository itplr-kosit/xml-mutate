package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutator.Mutator;
import lombok.extern.slf4j.Slf4j;

/**
 * Mutates the original document.
 *
 * @author Andreas Penski
 */
@Slf4j
public class MutateAction implements RunAction {

    @Override
    public void run(final Mutation mutation) {
        log.debug(
                "Running mutation {} on element {}", mutation.getMutator().getName(),
                mutation.getContext().getTarget().getNodeName());
        final Mutator mutator = mutation.getMutator();
        // mutator.mutate(mutation.getContext(), mutation.getConfiguration());
        // mutation.setState(State.MUTATED);
    }

    @Override
    public void run(RunnerDocumentContext context) {
        log.debug("Running mutation {} on element {}");
    }
}
