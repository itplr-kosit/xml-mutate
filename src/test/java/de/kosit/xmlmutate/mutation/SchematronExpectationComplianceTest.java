package de.kosit.xmlmutate.mutation;

import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation.ExpectedResult;
import de.kosit.xmlmutate.mutation.MutationResult.ExpectationCompliance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oclc.purl.dsdl.svrl.FiredRule;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Andreas Penski
 */
public class SchematronExpectationComplianceTest {

    @Test
    @DisplayName("Testen einen leere Schematron Ergebnisse/Vorgaben")
    public void testEmpty() {
        final MutationResult result = new MutationResult();
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.NOT_AVAILABLE);
        result.setSchematronExpectationMatches(Collections.emptyMap());
        result.setSchematronResult(Collections.emptyMap());
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.NOT_AVAILABLE);
    }

    @Test
    @DisplayName("Test Simple Compliance mit positiver Erwartung")
    public void testSimpleCompliant() {
        final MutationResult result = new MutationResult();
        final Map<SchematronRuleExpectation, Boolean> exp = new HashMap<>();
        exp.put(new SchematronRuleExpectation("test", "test", ExpectedResult.PASS), true);
        result.setSchematronExpectationMatches(exp);
        final Map<Schematron, SchematronOutput> schematronResult = new HashMap<>();
        final Schematron s = new Schematron("test", URI.create("urn:test"), Arrays.asList("BR-DE-1", "BR-DE-2", "BR-DE-3"));
        final SchematronOutput output = new SchematronOutput();
        output.getFiredRules().add(new FiredRule());
        schematronResult.put(s, output);
        result.setSchematronResult(schematronResult);
        assertThat(result.isSchematronExpectationCompliant()).isEqualTo(ExpectationCompliance.NOT_AVAILABLE);
    }
}
