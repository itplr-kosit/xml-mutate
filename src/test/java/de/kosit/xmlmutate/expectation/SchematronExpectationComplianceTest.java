package de.kosit.xmlmutate.expectation;

import de.kosit.xmlmutate.mutation.MutationResult;
import de.kosit.xmlmutate.mutation.MutationResult.ExpectationCompliance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Andreas Penski
 * @author Victor del Campo
 */
public class SchematronExpectationComplianceTest {

    @Test
    @DisplayName("Test with EMPTY schematron expectations")
    public void testEmpty() {
        final MutationResult result = new MutationResult();
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.NOT_AVAILABLE);
        result.setSchematronExpectationMatches(Collections.emptyMap());
        result.setSchematronResult(Collections.emptyMap());
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.NOT_AVAILABLE);
    }

    @Test
    @DisplayName("Test with a POSITIVE expectation being met")
    public void testSimpleCompliantPositiveExp() {
        final MutationResult result = new MutationResult();
        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.PASS), true);
        result.setSchematronExpectationMatches(exp);
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.COMPLIANT);
    }

    @Test
    @DisplayName("Test with a NEGATIVE expectation being met")
    public void testSimpleCompliantNegativeExp() {
        final MutationResult result = new MutationResult();
        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.FAIL), true);
        result.setSchematronExpectationMatches(exp);
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.COMPLIANT);
    }

    @Test
    @DisplayName("Test with a POSITIVE expectation NOT being met")
    public void testSimpleNonCompliantPositiveExp() {
        final MutationResult result = new MutationResult();
        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.PASS), false);
        result.setSchematronExpectationMatches(exp);
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.NOT_COMPLIANT);
    }

    @Test
    @DisplayName("Test with a NEGATIVE expectation NOT being met")
    public void testSimpleNonCompliantNegativeExp() {
        final MutationResult result = new MutationResult();
        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.FAIL), false);
        result.setSchematronExpectationMatches(exp);
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.NOT_COMPLIANT);
    }
}
