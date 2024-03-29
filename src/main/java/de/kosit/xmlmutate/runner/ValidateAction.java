// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.validation.Schema;
import org.apache.commons.lang3.StringUtils;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Validator validating against XSD and Schematron.
 *
 * @author Andreas Penski
 */
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
        final String piId = StringUtils.isBlank(mutation.getConfiguration().getMutationId()) ?
            StringUtils.EMPTY : mutation.getConfiguration().getMutationId();
        for (final Schematron schematron : getSchematronFiles()) {
            this.log.debug("Start validating {} with mutation {} using schematron {}",
                mutation.getIdentifier(), piId, schematron.getName());
            // Add unknown rules to this schematron file
            unknownRules.addAll(mutation.getConfiguration().getSchematronExpectations().stream().filter(n -> !schematron.hasRule(n)).toList());
            try {
                final SchematronOutput out = Services.getSchematronService().validate(schematron.getUri(), mutation.getContext().getDocument());
                this.log.debug("Mutation {} result={}", piId, out.getText());
                // add failed rules that were also declared, to this schematron file
                if (mutation.getConfiguration().getSchematronEnterityExpectation() == null) {
                    failedRules.addAll(getDeclaredFailedRules(schematron.getName(), out, mutation.getConfiguration().getSchematronExpectations()));
                } else {
                    failedCount += out.getFailedAsserts().size();
                }
                mutation.getResult().addSchematronResult(schematron, out);
            } catch (final CommonException e) {
                this.log.debug("Mutation {} Schematron validation runtime error={}", piId, e.getMessage());
                mutation.getMutationErrorContainer().addGlobalErrorMessage(new MutationException(ErrorCode.SCHEMATRON_EVALUATION_ERROR, e.getMessage()));
                // Set validation state
                mutation.getResult().setSchematronValidationState(ValidationState.UNPROCESSED);
                mutation.setState(State.ERROR);
            }
        }
        // Check which unknown rules were unknown to all schematron files
        final List<SchematronRuleExpectation> actualUnknowns = unknownRules.stream().filter(e -> Collections.frequency(unknownRules, e) > 1).distinct().toList();
        // Set validation state
        final boolean unknownRulesPresent = getSchematronFiles().size() > 1 ? !actualUnknowns.isEmpty() : !unknownRules.isEmpty();
        final boolean failedRulesPresent = mutation.getConfiguration().getSchematronEnterityExpectation() == null ? !failedRules.isEmpty() : failedCount != 0;
        final ValidationState schematronValidationState = failedRulesPresent || unknownRulesPresent ? ValidationState.INVALID : ValidationState.VALID;
        mutation.getResult().setSchematronValidationState(schematronValidationState);
        this.log.debug("Finished validating {} with mutation {}. Failed mutation asserts={}",
            mutation.getIdentifier(), piId, failedRules.isEmpty() ? failedCount : failedRules.size());
    }

    private List<SchematronRuleExpectation> getDeclaredFailedRules(final String schematronName, final SchematronOutput out, final List<SchematronRuleExpectation> expectations) {
        final List<SchematronRuleExpectation> declaredFailedRules = new ArrayList<>();
        final List<String> ruleNamesFailed = out.getFailedAsserts().stream().map(FailedAssert::getId).toList();
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
                final Document document = ObjectFactory.createDocumentBuilder(false).parse(this.targetFolder.resolve(mutation.getResultDocument()).toFile());
                final Result<Boolean, SyntaxError> result = Services.getSchemaValidatonService().validate(this.schema, document);
                mutation.addSchemaErrorMessages(result.getErrors());
                if (result.isInvalid()) {
                    mutation.setState(State.ERROR);
                    log.debug("Schema valid={}", result.isValid());
                }
                final ValidationState schemaValidationState = result.isValid() ? ValidationState.VALID : ValidationState.INVALID;
                mutation.getResult().setSchemaValidationState(schemaValidationState);
                if (mutation.getConfiguration().getSchemaValidationExpectation() != null) {
                    mutation.getResult().setSchemaValidationAsExpected(mutation.getConfiguration().getSchemaValidationExpectation().meetsValidationState(schemaValidationState));
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

    public ValidateAction(final Schema schema, final List<Schematron> schematronFiles, final Path targetFolder) {
        this.schema = schema;
        this.schematronFiles = schematronFiles;
        this.targetFolder = targetFolder;
    }

    Logger getLog() {
        return this.log;
    }

    Schema getSchema() {
        return this.schema;
    }

    List<Schematron> getSchematronFiles() {
        return this.schematronFiles;
    }

    Path getTargetFolder() {
        return this.targetFolder;
    }
}
