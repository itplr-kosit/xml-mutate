package de.kosit.xmlmutate.expectation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutation.MutationResult;

/**
 * @author Victor del Campo
 */
public class SchemaExpectationComplianceTest {

    @Test
    @DisplayName("Test with EMPTY schema expectation")
    public void testEmpty() {
        final MutationResult result = new MutationResult();
        assertThat(result.isSchemaExpectationCompliant()).isEqualTo(MutationResult.ExpectationCompliance.NOT_AVAILABLE);
    }


    @Test
    @DisplayName("Test with a POSITIVE expectation being met")
    public void testSimpleCompliantPositiveExp() {
        final MutationResult result = new MutationResult();
        result.setSchemaValidationAsExpected(true);
        result.setSchemaValidationState(MutationResult.ValidationState.VALID);
        assertThat(result.isSchemaExpectationCompliant()).isEqualTo(MutationResult.ExpectationCompliance.COMPLIANT);
    }


    @Test
    @DisplayName("Test with a NEGATIVE expectation being met")
    public void testSimpleCompliantNegativeExp() {
        final MutationResult result = new MutationResult();
        result.setSchemaValidationAsExpected(true);
        result.setSchemaValidationState(MutationResult.ValidationState.INVALID);
        assertThat(result.isSchemaExpectationCompliant()).isEqualTo(MutationResult.ExpectationCompliance.COMPLIANT);
    }

    @Test
    @DisplayName("Test with a POSITIVE expectation NOT being met")
    public void testSimpleNonCompliantPositiveExp() {
        final MutationResult result = new MutationResult();
        result.setSchemaValidationAsExpected(false);
        result.setSchemaValidationState(MutationResult.ValidationState.INVALID);
        assertThat(result.isSchemaExpectationCompliant()).isEqualTo(MutationResult.ExpectationCompliance.NOT_COMPLIANT);
    }

    @Test
    @DisplayName("Test with a NEGATIVE expectation NOT being met")
    public void testSimpleNonCompliantNegativeExp() {
        final MutationResult result = new MutationResult();
        result.setSchemaValidationAsExpected(false);
        result.setSchemaValidationState(MutationResult.ValidationState.VALID);
        assertThat(result.isSchemaExpectationCompliant()).isEqualTo(MutationResult.ExpectationCompliance.NOT_COMPLIANT);
    }




}
