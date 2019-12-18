package de.kosit.xmlmutate.mutation;

import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutator.Mutator;
import de.kosit.xmlmutate.runner.MutationException;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
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

    private MutationErrorContainer mutationErrorContainer = new MutationErrorContainer();

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
        this.result.setSchemaValidationAsExpected(isSchemaValidationAsExpected());
    }

    public void setState(State state) {
        if (state == null) {
            this.state = State.ERROR;
            throw new IllegalArgumentException("State should not be set to null");
        }
        this.state = state;
    }

    public void addSchemaErrorMessages(final Collection<SyntaxError> syntaxErrors) {
        this.result.getSchemaValidationErrors().addAll(syntaxErrors);
        syntaxErrors.forEach(e -> this.mutationErrorContainer.addSchemaErrorMessage(new MutationException(ErrorCode.SCHEMA_ERROR, e.getMessage())));
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
        return configuration.getSchemaValidationExpectation() != null && configuration.getSchemaValidationExpectation().meetsValidationState(this.result.getSchemaValidationState());
    }

    public boolean isSchemaExpectationSet() {
        return configuration.getSchemaValidationExpectation() != null;
    }

    public boolean isSchematronExpectationSet() {
        return !configuration.getSchematronExpectations().isEmpty();
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

    public boolean isAtLeastOneUnprocessed() {
        return !this.result.isSchemaProcessed() || !result.isSchematronProcessed();
    }

    public boolean hasUnexpectedValidation() {
        return !isAllAsExpected() && !isOneAsExpectedAndOneUnprocessed();
    }

    public boolean isErroneous() {
        return this.state == State.ERROR;
    }

    public boolean isErroneousOrContainsErrorMessages() {
        return this.state == State.ERROR || this.mutationErrorContainer.hasAnyErrors();
    }

    public boolean isOneAsExpectedAndOneUnprocessed() {
        return (this.result.isSchemaValidationAsExpected() && !this.result.isSchematronProcessed())
                || (this.result.allSchematronRulesAsExpected() && !this.result.isSchemaProcessed());
    }


    public enum State {
        ERROR, CREATED, MUTATED, VALIDATED, CHECKED
    }

}
