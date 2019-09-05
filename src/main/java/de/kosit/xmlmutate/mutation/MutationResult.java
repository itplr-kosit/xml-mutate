package de.kosit.xmlmutate.mutation;

import de.init.kosit.commons.SyntaxError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import java.util.*;
import java.util.Map.Entry;

/**
 * Mutationsergebnis aus den diversen Schritten.
 *
 * @author Andreas Penski
 */
@Getter
@Setter
public class MutationResult {

    private Map<Schematron, SchematronOutput> schematronResult = new HashMap<>();

    private List<SyntaxError> schemaValidationErrors = new ArrayList<>();

    private ValidationState schemaValidation = ValidationState.UNPROCESSED;

    private ValidationState schematronValidation = ValidationState.UNPROCESSED;

    private Map<SchematronRuleExpectation, Boolean> schematronExpectationMatches = new HashMap<>();

    public boolean isAllValid() {
        return isSchemaValid() && allSchematronRulesAsExpected();
    }

    public boolean isSchemaProcessed() {
        return !this.schematronValidation.equals(ValidationState.UNPROCESSED);
    }

    public boolean isSchemaValid() {
        return this.schemaValidation.equals(ValidationState.VALID)
                || this.schemaValidation.equals(ValidationState.UNPROCESSED);
    }

    /**
     * Prüft ob die Mutation keine Schemavalidierung hatte = UNPROCESSED
     *
     * @return ob UNPROCESSED oder nicht
     */
    public boolean isUnprocessed() {
        return this.schemaValidation.equals(ValidationState.UNPROCESSED);
    }

    public boolean isSchematronProcessed() {
        return !this.schematronValidation.equals(ValidationState.UNPROCESSED);
    }

    /**
     * Evaluiert den
     *
     * @return
     */
    public boolean allSchematronRulesAsExpected() {
        return this.schematronExpectationMatches.size() > 0 && this.schematronExpectationMatches.entrySet().stream().allMatch(Entry::getValue);
    }

    public void addSchematronResult(final Schematron schematron, final SchematronOutput out) {
        this.schematronResult.put(schematron, out);
    }

    /**
     * @param schematronSource
     * @return
     */
    public Optional<SchematronOutput> getSchematronResult(final String schematronSource) {
        return this.schematronResult.entrySet().stream().filter(e -> e.getKey().getName().equals(schematronSource))
                .map(Entry::getValue).findFirst();
    }

    @Getter
    @RequiredArgsConstructor
    public enum ValidationState {

        UNPROCESSED(""), VALID("OK"), INVALID("FAILED");

        private final String text;
    }
}
