package de.kosit.xmlmutate.mutation;

import de.kosit.xmlmutate.mutator.Mutator;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Sammelobjekt für eine Mutation innerhalb einer Test-Datei. Sammelt alle
 * möglichen Informationen rund um die Verarbeitung, inkl. Testergebnisse.
 *
 * @author Andreas Penski
 * @author Renzo Kottmann
 */

@Getter

public class Mutation {

    private final MutationContext context;

    private String identifier;

    private MutationConfig configuration = new MutationConfig();

    private Mutator mutator;

    private MutationResult result = new MutationResult();

    private State state = State.CREATED;

    private Map<String, String> errorMessages = new HashMap<>();

    /**
     * Constructor.
     *
     * @param context    der {@link MutationContext}
     * @param identifier ein Name / Bezeichner für die Mutation
     */
    public Mutation(final MutationContext context, final String identifier) {
        this.context = context;
        this.identifier = identifier;
    }

    public Mutation(final MutationContext context, final String identifier, MutationConfig configuration) {
        this(context, identifier);
        this.configuration = configuration;
    }

    public Mutation(final MutationContext context, final String identifier, MutationConfig configuration,
                    Mutator mutator) {
        this(context, identifier, configuration);
        this.mutator = mutator;
        this.result.setSchemaValidationAsExpected(configuration.isSchemaValidationAsExpected());
    }

    public void setState(State state) {
        if (state == null) {
            this.state = State.ERROR;
            throw new IllegalArgumentException("State should not be set to null");
        }
        this.state = state;
    }

    public void addErrorMessage(String ruleName, String message) {
        this.errorMessages.put(ruleName, message);
    }

    public Path getResultDocument() {
        return Paths.get(getContext().getDocumentName()).resolve(this.identifier + ".xml");
    }

    public boolean isSchemaValid() {
        return this.result.isSchemaValid();
    }

    public boolean isSchematronValid() {
        return this.result.isSchematronValid();
    }

    public boolean isSchemaProcessed() {
        return result.isSchemaProcessed();
    }

    public boolean isSchematronProcessed() {
        return result.isSchematronProcessed();
    }

    public boolean isSchemaValidationAsExpected() {
        return configuration.isSchemaValidationAsExpected();
    }


    public boolean isAllAsExpected() {
        return result.isExpectationCompliant();
    }

    /**
     * Prüft ob eine Mutation Schema- und Schematron-validiert wurde
     *
     * @return ob eine Mutation Schema- und Schematron-validiert wurde
     */
    public boolean isAllUnprocessed() {
        return !this.result.isSchemaProcessed() && !result.isSchematronProcessed();
    }

    public boolean hasUnexpectedValidation() {
        return !isAllAsExpected();
    }

    public boolean isErroneous() {
        return this.state == State.ERROR;
    }


    public enum State {
        ERROR, CREATED, MUTATED, VALIDATED, CHECKED;
    }

}
