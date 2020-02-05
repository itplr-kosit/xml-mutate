package de.kosit.xmlmutate.assertions;

import org.assertj.core.api.AbstractAssert;

import de.kosit.xmlmutate.runner.RunnerResult;

/**
 * An Assert for asserting things in {@link RunnerResult}.
 * 
 * @author Andreas Penski
 */
public class RunnerResultAssert extends AbstractAssert<RunnerResultAssert, RunnerResult> {

    /**
     * Constructor.
     *
     * @param result the mutation results to test.
     */
    RunnerResultAssert(final RunnerResult result) {
        super(result, RunnerResultAssert.class);
    }

    /**
     * Ensures that this node is an attribute
     *
     * @return this
     */
    public RunnerResultAssert isErroneous() {
        if (!this.actual.isErrorPresent()) {
            failWithMessage("Expected result to be erroneous");
        }
        return this;
    }

    public RunnerResultAssert hasMutationCount(final int expected) {
        final long count = this.actual.getResult().stream().flatMap(e -> e.getValue().stream()).count();
        if (count != expected) {
            failWithMessage("Expected to have exactly %s mutations. Found %s", expected, count);
        }
        return this;
    }

    public RunnerResultAssert isSucessful() {
        if (!this.actual.isSuccessful()) {
            failWithMessage("Expected result to be successful. It's not!");
        }
        return this;
    }
}
