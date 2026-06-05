package de.kosit.xmlmutate.assertions;

import de.kosit.xmlmutate.runner.RunnerResult;

/**
 * Runner-side extensions of the AssertJ assertions. Import statically alongside
 * {@link Assertions} for tests that exercise {@link RunnerResult}.
 */
public class RunnerAssertions {

    /**
     * Assertion for {@link RunnerResult}, which allows deeply inspecting the runner result.
     *
     * @param actual the result object
     * @return the {@link RunnerResultAssert}
     */
    public static RunnerResultAssert assertThat(final RunnerResult actual) {
        return new RunnerResultAssert(actual);
    }
}
