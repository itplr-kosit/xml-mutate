package de.kosit.xmlmutate.mutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.oclc.purl.dsdl.svrl.SchematronOutput;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import de.init.kosit.commons.SyntaxError;

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

    private Map<Expectation, Boolean> expectationResult = new HashMap<>();

    public boolean isValid() {
        return isSchemaValid() && isExpectationCompliant();
    }

    public boolean isSchemaValid() {
        return this.schemaValidation == ValidationState.VALID || this.schemaValidation == ValidationState.UNPROCESSED;
    }

    public boolean isSchematronValid() {
        return this.schematronValidation == ValidationState.VALID || this.schematronValidation == ValidationState.UNPROCESSED;
    }

    public void addSchematronResult(final Schematron schematron, final SchematronOutput out) {
        this.schematronResult.put(schematron, out);
    }

    /**
     * Evaluiert den
     * 
     * @return
     */
    public boolean isExpectationCompliant() {
        return this.expectationResult.entrySet().stream().allMatch(Entry::getValue);
    }

    /**
     *
     * @param schematronSource
     * @return
     */
    public Optional<SchematronOutput> getSchematronResult(final String schematronSource) {
        return this.schematronResult.entrySet().stream().filter(e -> e.getKey().getName().equals(schematronSource)).map(Entry::getValue)
                                    .findFirst();
    }

    @Getter
    @RequiredArgsConstructor
    public enum ValidationState {

        UNPROCESSED(""), VALID("OK"), INVALID("FAILED");

        private final String text;
    }
}
