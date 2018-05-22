package de.kosit.xmlmutate.tester;

/**
 * Expectation
 */
public interface Expectation {
    /**
     * An expectation on a subject e.g. name of a Schematron result, or XML Schema
     */
    public String on();

    /**
     * Of() what do we expect something e.g. which Schematron rule do we expect to be true/or false
     */
    public String what();

    /**
     * Is the expectation true or false?
     */
    public boolean is();
}