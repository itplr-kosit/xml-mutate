package de.kosit.xmlmutate.mutation;


/**
 * Expectation on the validation after mutation of a Schematron Rule Schematron-
 *
 * @author Renzo Kottmann
 * @author Andreas Penski
 */

public class SchematronRuleExpectation {

    /**
     * The symbolic short name of the Schematron source.
     */
    private final String source;

    /**
     * Name of schematron rule on which expectation is expressed.
     */
    private final String ruleName;

    /**
     * The actual expectation value.
     */
    private final ExpectedResult expectedResult;

    private SchematronRuleExpectation() {
        source = "";
        ruleName = "";
        expectedResult = ExpectedResult.UNDEFINED;
    }

    public SchematronRuleExpectation(String schematronSource, String ruleName, ExpectedResult expectation) {
        this.source = schematronSource;
        this.ruleName = ruleName;
        this.expectedResult = expectation;
    }

    /**
     * An expectation on a subject e.g. name of a Schematron result, or XML Schema
     */
    public String getSource() {
        return this.source;
    };

    /**
     * Of() what do we expect something e.g. which Schematron rule do we expect to
     * be true/or false
     */
    public String getRuleName() {
        return this.ruleName;
    };

    /**
     * Is expected to fail?
     */
    public boolean mustFail() {
        return ExpectedResult.FAIL.equals(this.expectedResult);
    }

    /**
     * Is the expected to pass?
     */
    public boolean mustPass() {
        return ExpectedResult.PASS.equals(this.expectedResult);
    }

    public enum ExpectedResult {
        FAIL, PASS, UNDEFINED
    }
}
