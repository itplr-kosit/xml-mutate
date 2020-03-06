package de.kosit.xmlmutate.expectation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutation.MutationResult;

/**
 * @author Victor del Campo
 */
public class OverallExpectationComplianceTest {

    @Test
    @DisplayName("Test with EMPTY overall expectation")
    public void testEmpty() {
        final MutationResult result = new MutationResult();
        assertThat(result.isExpectationCompliant()).isFalse();
    }


    @Test
    @DisplayName("Test with a POSITIVE expectations being met")
    public void testSimpleCompliantPositiveExp() {
        final MutationResult result = new MutationResult();

        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.PASS), true);
        result.setSchematronExpectationMatches(exp);

        result.setSchemaValidationAsExpected(true);
        result.setSchemaValidationState(MutationResult.ValidationState.VALID);

        assertThat(result.isExpectationCompliant()).isTrue();
    }


    @Test
    @DisplayName("Test with NEGATIVE expectations being met")
    public void testSimpleCompliantNegativeExp() {
        final MutationResult result = new MutationResult();

        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.FAIL), true);
        result.setSchematronExpectationMatches(exp);

        result.setSchemaValidationAsExpected(true);
        result.setSchemaValidationState(MutationResult.ValidationState.INVALID);

        assertThat(result.isExpectationCompliant()).isTrue();
    }


    @Test
    @DisplayName("Test with a POSITIVE expectations NOT being met")
    public void testSimpleNonCompliantPositiveExp() {
        final MutationResult result = new MutationResult();

        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.PASS), false);
        result.setSchematronExpectationMatches(exp);

        result.setSchemaValidationAsExpected(false);
        result.setSchemaValidationState(MutationResult.ValidationState.INVALID);

        assertThat(result.isExpectationCompliant()).isFalse();
    }


    @Test
    @DisplayName("Test with a NEGATIVE expectations NOT being met")
    public void testSimpleNonCompliantNegativeExp() {
        final MutationResult result = new MutationResult();

        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.FAIL), false);
        result.setSchematronExpectationMatches(exp);

        result.setSchemaValidationAsExpected(false);
        result.setSchemaValidationState(MutationResult.ValidationState.VALID);

        assertThat(result.isExpectationCompliant()).isFalse();
    }
}
