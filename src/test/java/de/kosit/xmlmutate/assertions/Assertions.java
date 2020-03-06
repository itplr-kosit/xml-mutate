package de.kosit.xmlmutate.assertions;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.runner.RunnerResult;

/**
 * Useful extensions of the AssertJ assertions.
 *
 * @author Andreas Penski
 */
public class Assertions {

    /**
     * Assertion for {@link Node}, including {@link org.w3c.dom.Element}, {@link org.w3c.dom.Attr} and
     * {@link org.w3c.dom.Document}.
     *
     * @param actual der node that should be tested
     * @return {@link NodeAssert}
     */
    public static NodeAssert assertThat(final Node actual) {
        return new NodeAssert(actual);
    }

    /**
     * Assertion for {@link Node}, including {@link org.w3c.dom.Element}, {@link org.w3c.dom.Attr} and
     * {@link org.w3c.dom.Document}.
     *
     * @param actual der node that should be tested
     * @return {@link NodeAssert}
     */
    public static NodeListAssert assertThat(final NodeList actual) {
        return new NodeListAssert(actual);
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
