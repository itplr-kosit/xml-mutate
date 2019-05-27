package de.kosit.xmlmutate.runner;

import java.util.List;

import javax.xml.validation.Schema;

import org.oclc.purl.dsdl.svrl.SchematronOutput;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.init.kosit.commons.Result;
import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutation.Schematron;

/**
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
@Getter(AccessLevel.PACKAGE)
public class ValidateAction implements RunAction {

    private final Schema schema;

    private final List<Schematron> schematronRules;

    @Override
    public void run(final Mutation mutation) {
        log.info("validating {}", mutation.getIdentifier());
        schemaValidation(mutation);
        schematronValidation(mutation);
        mutation.setState(State.VALIDATED);
    }

    private void schematronValidation(final Mutation mutation) {
        long failedAssertCount = 0;

        for (final Schematron s : getSchematronRules()) {
            final SchematronOutput out = Services.schematronService.validate(s.getUri(), mutation.getContext().getDocument());
            failedAssertCount += out.getFailedAsserts().size();
            mutation.getResult().addSchematronResult(s, out);
        }
        mutation.getResult().setSchematronValidation(failedAssertCount > 0 ? ValidationState.INVALID : ValidationState.VALID);
    }

    private void schemaValidation(final Mutation mutation) {
        if (this.schema != null) {
            final Result<Boolean, SyntaxError> result = Services.schemaValidatonService.validate(this.schema,
                                                                                                 mutation.getContext().getDocument());
            mutation.getResult().getSchemaValidationErrors().addAll(result.getErrors());
            mutation.getResult().setSchemaValidation(result.isValid() ? ValidationState.VALID : ValidationState.INVALID);
        }

    }
}
