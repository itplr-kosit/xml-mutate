package de.kosit.xmlmutate.mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.tester.Expectation;

/**
 * @author Andreas Penski
 */
@Getter
@Setter
public class MutationResult {

    private List<String> schematronViolations;

    private List<SyntaxError> schemaValidationErrors = new ArrayList<>();

    private ValidationState schemaValidation = ValidationState.UNPROCESSED;

    private ValidationState schematronValidation = ValidationState.UNPROCESSED;

    private Map<Expectation, String> expectationViolations;

    public boolean isValid() {
        return isSchemaValid() && isSchematronValid();
    }

    private boolean isSchemaValid() {
        return this.schemaValidation == ValidationState.VALID;
    }

    public boolean isSchematronValid() {
        return this.schematronValidation == ValidationState.VALID;
    }

    public enum ValidationState {
        UNPROCESSED, VALID, INVALID
    }
}
