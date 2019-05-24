package de.kosit.xmlmutate.mutation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Expectation
 */
@AllArgsConstructor
@Getter
public class Expectation {

    private final String schematronSource;

    private final String ruleName;

    private final boolean expectedResult;

    /**
     * An expectation on a subject e.g. name of a Schematron result, or XML Schema
     */
    public String schematron() {
        return this.schematronSource;
    };

    /**
     * Of() what do we expect something e.g. which Schematron rule do we expect to be true/or false
     */
    public String ruleName() {
        return this.ruleName;
    };

    /**
     * Is the expectation true or false?
     */
    public boolean mustFail() {
        return this.expectedResult;
    }
}