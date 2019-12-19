package de.kosit.xmlmutate.assertions;

import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.runner.RunnerResult;

/**
 * Nützliche Erweiterungen der AssertJ assertions.
 *
 * @author Andreas Penski
 */
public class Assertions {

    /**
     * Assertion für {@link Node}, inklusive {@link org.w3c.dom.Element}, {@link org.w3c.dom.Attr} und
     * {@link org.w3c.dom.Document}.
     *
     * @param actual der Knoten der getestet werden soll
     * @return {@link NodeAssert}
     */
    public static NodeAssert assertThat(final Node actual) {
        return new NodeAssert(actual);
    }

    /**
     * Assertion for {@link RunnerResult}, which allows deeply inspect the mutator resut.
     *
     * @param actual the result object
     * @return the {@link RunnerResultAssert}
     */
    public static RunnerResultAssert assertThat(final RunnerResult actual) {
        return new RunnerResultAssert(actual);
    }

    /**
     * Assertion for {@link de.kosit.xmlmutate.mutation.Mutation}, which allows deeply inspect the mutator resut.
     *
     * @param actual the mutation
     * @return the {@link RunnerResultAssert}
     */
    public static MutationAssert assertThat(final Mutation actual) {
        return new MutationAssert(actual);
    }
}
