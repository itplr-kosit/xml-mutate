package de.kosit.xmlmutate.mutation;

import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Mutationsergebnis aus den diversen Schritten.
 *
 * @author Andreas Penski
 */
@Getter
@Setter
public class MutationResult {

    // Validation states (Schema and Schematron)
    private ValidationState schemaValidationState = ValidationState.UNPROCESSED;
    private ValidationState schematronValidationState = ValidationState.UNPROCESSED;

    // Schema error and expectation match
    private boolean schemaValidationAsExpected;
    private List<SyntaxError> schemaValidationErrors = new ArrayList<>();

    // Schematron expectations matches and schematron result
    private Map<SchematronRuleExpectation, Boolean> schematronExpectationMatches = new HashMap<>();
    private Map<Schematron, SchematronOutput> schematronResult = new HashMap<>();
    public void addSchematronResult(final Schematron schematron, final SchematronOutput out) {
        this.schematronResult.put(schematron, out);
    }
    public Optional<SchematronOutput> getSchematronResult(final String schematronSource) {
        return this.schematronResult.entrySet().stream().filter(e -> e.getKey().getName().equals(schematronSource))
                .map(Entry::getValue).findFirst();
    }


    // CHECK IF PROCESSED
    boolean isSchemaProcessed() {
        return !this.schematronValidationState.equals(ValidationState.UNPROCESSED);
    }
    boolean isSchematronProcessed() {
        return !this.schematronValidationState.equals(ValidationState.UNPROCESSED);
    }

    // CHECK IF VALID
    boolean isSchemaValid() {
        return this.schemaValidationState.equals(ValidationState.VALID)
                || this.schemaValidationState.equals(ValidationState.UNPROCESSED);
    }
    boolean isSchematronValid() {
        return this.schematronValidationState.equals(ValidationState.VALID)
                || this.schemaValidationState.equals(ValidationState.UNPROCESSED);

    }


    // CHECK IF EXPECTATION COMPLIANT
    public enum ExpectationCompliance {
        NOT_AVAILABLE, COMPLIANT, NOT_COMPLIANT
    }
    public boolean isExpectationCompliant() {
        final ExpectationCompliance schematronExpectationCompliant = isSchematronExpectationCompliant();
        final ExpectationCompliance schemaExpectationCompliant = isSchemaExpectationCompliant();
        return ExpectationCompliance.COMPLIANT.equals(schematronExpectationCompliant)
                && ExpectationCompliance.COMPLIANT.equals(schemaExpectationCompliant);
    }
    public ExpectationCompliance isSchematronExpectationCompliant() {
        if (this.schematronExpectationMatches.isEmpty()) {
            return ExpectationCompliance.NOT_AVAILABLE;
        }
        return allSchematronRulesAsExpected() ? ExpectationCompliance.COMPLIANT : ExpectationCompliance.NOT_COMPLIANT;
    }
    public ExpectationCompliance isSchemaExpectationCompliant() {
        if (this.schemaValidationState == ValidationState.UNPROCESSED) {
            return ExpectationCompliance.NOT_AVAILABLE;
        }
        return isSchemaValidationAsExpected() ? ExpectationCompliance.COMPLIANT : ExpectationCompliance.NOT_COMPLIANT;
    }
    boolean allSchematronRulesAsExpected() {
        return !this.schematronExpectationMatches.isEmpty() && this.schematronExpectationMatches.entrySet().stream().allMatch(Entry::getValue);
    }



    @Getter
    @RequiredArgsConstructor
    public enum ValidationState {
        UNPROCESSED(""), VALID("OK"), INVALID("FAILED");
        private final String text;
    }

    public List<SchematronRuleExpectation> getFailedSchematronExpectations() {
        return this.schematronExpectationMatches.entrySet()
                .stream().filter(e -> Boolean.FALSE.equals(e.getValue())).map(Entry::getKey)
                .collect(Collectors.toList());
    }
}
