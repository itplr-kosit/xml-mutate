package de.kosit.xmlmutate.mutation;

import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.mutator.Mutator;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

    private Map<String, String> schematronErrorMessages = new HashMap<>();

    private List<String> schemaErrorMessages = new ArrayList<>();

    private List<String> globalErrorMessages = new ArrayList<>();

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
    }

    public void setState(State state) {
        if (state == null) {
            this.state = State.ERROR;
            throw new IllegalArgumentException("State should not be set to null");
        }
        this.state = state;
    }

    public void addSchematronErrorMessage(final String ruleName, final String message) {
        this.schematronErrorMessages.put(ruleName, message);
    }

    public void addSchemaErrorMessages(final Collection<SyntaxError> syntaxErrors) {
        this.result.getSchemaValidationErrors().addAll(syntaxErrors);
        this.schemaErrorMessages.addAll(syntaxErrors.stream().map(SyntaxError::getMessage).collect(Collectors.toList()));
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

    public boolean isSchemaValidationAsExpected() {
        return configuration.getSchemaValidationExpectation() != null && configuration.getSchemaValidationExpectation().meetsValidationState(this.result.getSchemaValidation());
    }

    public boolean isSchematronProcessed() {
        return result.isSchematronProcessed();
    }

    public boolean isAllAsExpected() {

        return this.isSchemaValidationAsExpected() && result.allSchematronRulesAsExpected();
    }

    /**
     * Prüft ob eine Mutation Schema- und Schematron-validiert wurde
     *
     * @return ob eine Mutation Schema- und Schematron-validiert wurde
     */
    public boolean isAllUnprocessed() {
        return this.result.isUnprocessed() && !result.isSchematronProcessed();
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
