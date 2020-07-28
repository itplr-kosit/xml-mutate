package de.kosit.xmlmutate.assertions;

import org.assertj.core.api.AbstractAssert;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * @author Andreas Penski
 */
public class MutationAssert extends AbstractAssert<MutationAssert, Mutation> {

    /**
     * Constructor.
     *
     * @param result the mutation results to test.
     */
    MutationAssert(final Mutation result) {
        super(result, MutationAssert.class);
    }

    public MutationAssert containsError(final String s) {
        if (this.actual.getMutationErrorContainer().getAllErrorMessages().stream().noneMatch(e -> e.contains(s))) {
            failWithMessage("Expected to contain error message %s but does not", s);
        }
        return this;
    }

}
