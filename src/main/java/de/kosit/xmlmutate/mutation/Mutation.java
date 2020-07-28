package de.kosit.xmlmutate.mutation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.lang3.RegExUtils;

import lombok.Getter;

import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.mutator.Mutator;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Collectible object for a mutation within a test file. It collects all possible
 * informations about the processing including the test results
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
        syntaxErrors.forEach(
                e -> this.mutationErrorContainer.addSchemaErrorMessage(new MutationException(ErrorCode.SCHEMA_ERROR, e.getMessage())));
    }

    public Path getResultDocument() {
        return Paths.get(getContext().getDocumentName()).resolve(
                RegExUtils.replacePattern(this.identifier, "[^a-zA-Z0-9\\\\.\\-_]", "_") + ".xml");
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
        return configuration.getSchemaValidationExpectation() != null && configuration.getSchemaValidationExpectation()
                                                                                      .meetsValidationState(
                                                                                              this.result.getSchemaValidationState());
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

    /**
     * Check if a mutation was neither schema nor schematron validated
     *
     * @return true or false
     */
    public boolean isAllUnprocessed() {
        return !this.result.isSchemaProcessed() && !result.isSchematronProcessed();
    }

    public enum State {
        ERROR, CREATED, MUTATED, VALIDATED, CHECKED
    }

}
