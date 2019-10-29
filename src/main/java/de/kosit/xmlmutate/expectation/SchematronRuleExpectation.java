package de.kosit.xmlmutate.expectation;

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
    private String source;

    /**
     * Name of schematron rule on which expectation is expressed.
     */
    private String ruleName;

    /**
     * The actual expectation value.
     */
    private ExpectedResult expectedResult;


    public SchematronRuleExpectation(final String schematronSource, final String ruleName, final ExpectedResult expectation) {
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
    public boolean expectInvalid() {
        return ExpectedResult.FAIL.equals(this.expectedResult);
    }

    /**
     * Is the expected to pass?
     */
    public boolean expectValid() {
        return ExpectedResult.PASS.equals(this.expectedResult);
    }

    public enum ExpectedResult {
        FAIL, PASS
    }
}
