package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;

/**
 * @author Andreas Penski
 */
public class CheckAction implements RunAction {

    @Override
    public void run(final Mutation mutation) {
        mutation.getConfiguration().getSchematronExpectations().forEach(e -> {
            mutation.getResult().getExpectationResult().put(e, e.evaluate(mutation.getResult()));
        });
        mutation.setState(State.CHECKED);
    }
}
