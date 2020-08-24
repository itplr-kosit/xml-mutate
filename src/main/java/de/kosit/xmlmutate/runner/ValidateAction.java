package de.kosit.xmlmutate.runner;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.Result;
import de.init.kosit.commons.SyntaxError;
import de.init.kosit.commons.util.CommonException;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutation.Schematron;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

        final List<SchematronRuleExpectation> unknownRules = new ArrayList<>();
        final List<SchematronRuleExpectation> failedRules = new ArrayList<>();
        int failedCount = 0;
        for (final Schematron schematron : getSchematronFiles()) {

            this.log.debug("Using schematron={}", schematron.getName());

            // Add unknown rules to this schematron file
            unknownRules.addAll(mutation.getConfiguration().getSchematronExpectations().stream().filter(n -> !schematron.hasRule(n)).collect(Collectors.toList()));
            try {
                final SchematronOutput out = Services.getSchematronService().validate(schematron.getUri(),
                        mutation.getContext().getDocument());
                this.log.debug("result={}", out.getText());
                // add failed rules that were also declared, to this schematron file
                if (mutation.getConfiguration().getSchematronEnterityExpectation() == null) {
                    failedRules.addAll(getDeclaredFailedRules(schematron.getName(), out, mutation.getConfiguration().getSchematronExpectations()));
                } else {
                    failedCount += out.getFailedAsserts().size();
                }
                mutation.getResult().addSchematronResult(schematron, out);

            } catch (final CommonException e) {
                this.log.debug("Schematron validation runtime error={}", e.getMessage());
                mutation.getMutationErrorContainer()
                        .addGlobalErrorMessage(new MutationException(ErrorCode.SCHEMATRON_EVALUATION_ERROR, e.getMessage()));
                // Set validation state
                mutation.getResult().setSchematronValidationState(ValidationState.UNPROCESSED);
                mutation.setState(State.ERROR);
            }

        }
        // Check which unknown rules were unknown to all schematron files
        final List<SchematronRuleExpectation> actualUnknowns = unknownRules.stream()
                .filter(e -> Collections.frequency(unknownRules, e) > 1)
                .distinct()
                .collect(Collectors.toList());
        // Set validation state
        final boolean unknownRulesPresent = getSchematronFiles().size() > 1 ?
                !actualUnknowns.isEmpty() : !unknownRules.isEmpty();
        final boolean failedRulesPresent = mutation.getConfiguration().getSchematronEnterityExpectation() == null ?
                !failedRules.isEmpty() : failedCount != 0;
        final ValidationState schematronValidationState = failedRulesPresent || unknownRulesPresent ? ValidationState.INVALID
                : ValidationState.VALID;
        mutation.getResult().setSchematronValidationState(schematronValidationState);

        this.log.debug("failed asserts={}", failedRules.isEmpty() ? failedCount : failedRules.size());

    }

    private List<SchematronRuleExpectation> getDeclaredFailedRules(final String schematronName, final SchematronOutput out, final List<SchematronRuleExpectation> expectations) {
        final List<SchematronRuleExpectation> declaredFailedRules = new ArrayList<>();
        final List<String> ruleNamesFailed = out.getFailedAsserts().stream().map(FailedAssert::getId).collect(Collectors.toList());
        for (final SchematronRuleExpectation e : expectations) {
            if (ruleNamesFailed.contains(e.getRuleName()) && e.getSource() != null && e.getSource().equalsIgnoreCase(schematronName)) {
                declaredFailedRules.add(e);
            }
        }
        return declaredFailedRules;
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
