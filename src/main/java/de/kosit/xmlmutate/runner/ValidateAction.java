package de.kosit.xmlmutate.runner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.validation.Schema;

import org.apache.commons.collections4.CollectionUtils;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.Result;
import de.init.kosit.commons.SyntaxError;
import de.init.kosit.commons.util.CommonException;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutation.Schematron;

/**
 * Validator validating against XSD and Schematron.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PACKAGE)
public class ValidateAction implements RunAction {

    final Logger log = LoggerFactory.getLogger(ValidateAction.class);

    private final Schema schema;

    private final List<Schematron> schematronFiles;

    private final Path targetFolder;

    @Override
    public void run(final Mutation mutation) {
        this.log.info("validating {}", mutation.getIdentifier());
        schemaValidation(mutation);
        schematronValidation(mutation);
        if (!mutation.getState().equals(State.ERROR)) {
            mutation.setState(State.VALIDATED);
        }
    }

    private void schematronValidation(final Mutation mutation) {

        long failedAssertCount = 0;
        boolean unknownRulenameExist = false;
        boolean failedRulesAreListed = false;
        final List<String> ruleNamesDeclared = mutation.getConfiguration().getSchematronExpectations().stream()
                .map(SchematronRuleExpectation::getRuleName).collect(Collectors.toList());

        for (final Schematron schematron : getSchematronFiles()) {

            // Check if unknown rule names were declared
            if (!unknownRulenameExist) {
                unknownRulenameExist = mutation.getConfiguration().getSchematronExpectations().stream()
                        .anyMatch(n -> !schematron.hasRule(n.getRuleName()));
            }

            this.log.debug("Using schematron={}", schematron.getName());

            try {
                final SchematronOutput out = Services.getSchematronService().validate(schematron.getUri(),
                        mutation.getContext().getDocument());
                this.log.debug("result={}", out.getText());
                failedAssertCount += out.getFailedAsserts().size();
                this.log.debug("failed asserts={}", failedAssertCount);
                mutation.getResult().addSchematronResult(schematron, out);

                // Check if failed rules were also declared
                if (!failedRulesAreListed) {
                    failedRulesAreListed = checkPresenceOfFailedRules(out, ruleNamesDeclared);
                }
                // Set validation state
                final ValidationState schematronValidationState = failedRulesAreListed || unknownRulenameExist ? ValidationState.INVALID
                        : ValidationState.VALID;
                mutation.getResult().setSchematronValidationState(schematronValidationState);
            } catch (final CommonException e) {
                this.log.debug("Schematron validation runtime error={}", e.getMessage());
                mutation.getMutationErrorContainer()
                        .addGlobalErrorMessage(new MutationException(ErrorCode.SCHEMATRON_EVALUATION_ERROR, e.getMessage()));
                // Set validation state
                mutation.getResult().setSchematronValidationState(ValidationState.UNPROCESSED);
                mutation.setState(State.ERROR);
            }

        }

    }

    private boolean checkPresenceOfFailedRules(final SchematronOutput out, final List<String> ruleNamesDeclared) {
        final List<String> ruleNamesFailed = out.getFailedAsserts().stream().map(FailedAssert::getId).collect(Collectors.toList());
        return CollectionUtils.containsAny(ruleNamesDeclared, ruleNamesFailed);
    }

    private void schemaValidation(final Mutation mutation) {
        if (this.schema != null) {
            try {
                final Document document = ObjectFactory.createDocumentBuilder(false)
                        .parse(this.targetFolder.resolve(mutation.getResultDocument()).toFile());
                final Result<Boolean, SyntaxError> result = Services.getSchemaValidatonService().validate(this.schema, document);
                mutation.addSchemaErrorMessages(result.getErrors());
                if (result.isInvalid()) {
                    mutation.setState(State.ERROR);
                    log.debug("Schema valid={}", result.isValid());
                }
                final ValidationState schemaValidationState = result.isValid() ? ValidationState.VALID : ValidationState.INVALID;
                mutation.getResult().setSchemaValidationState(schemaValidationState);
                if (mutation.getConfiguration().getSchemaValidationExpectation() != null) {
                    mutation.getResult().setSchemaValidationAsExpected(
                            mutation.getConfiguration().getSchemaValidationExpectation().meetsValidationState(schemaValidationState));
                }
            } catch (final SAXException e) {
                mutation.setState(State.ERROR);
                mutation.getMutationErrorContainer().addGlobalErrorMessage(new MutationException(ErrorCode.INVALID_MUTATION_PRODUCED));
            } catch (final IOException e) {
                mutation.setState(State.ERROR);
                mutation.getMutationErrorContainer().addGlobalErrorMessage(new MutationException(ErrorCode.MUTATION_XML_FILE_READ_PROBLEM));
            }
        }

    }
}
