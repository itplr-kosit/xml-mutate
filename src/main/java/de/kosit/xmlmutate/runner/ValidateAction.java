package de.kosit.xmlmutate.runner;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.Result;
import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.mutation.SchematronRuleExpectation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.nio.file.Path;
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
        mutation.setState(State.VALIDATED);
    }

    private void schematronValidation(final Mutation mutation) {

        long failedAssertCount = 0;
        boolean unknownRulenameExist = false;
        boolean failedRulesAreListed = false;
        final List<String> ruleNamesDeclared = mutation.getConfiguration().getSchematronExpectations()
                .stream().map(SchematronRuleExpectation::getRuleName).collect(Collectors.toList());

        for (final Schematron schematron : getSchematronFiles()) {

            // Check if unknown rule names were declared
            if (!unknownRulenameExist) {
                unknownRulenameExist = mutation.getConfiguration().getSchematronExpectations().stream()
                        .anyMatch(n -> !schematron.hasRule(n.getRuleName()));
            }

            this.log.debug("Using schematron={}",schematron.getName());
            final SchematronOutput out = Services.getSchematronService().validate(schematron.getUri(), mutation.getContext().getDocument());
            this.log.debug("result={}",out.getText());
            failedAssertCount += out.getFailedAsserts().size();
            this.log.debug("failed asserts={}",failedAssertCount);
            mutation.getResult().addSchematronResult(schematron, out);

            // Check if failed rules were also declared
            if (!failedRulesAreListed) {
                failedRulesAreListed = checkPresenceOfFailedRules(out, ruleNamesDeclared);
            }


        }

        // Set validation state and expectation state
        final ValidationState schematronValidationState = failedRulesAreListed || unknownRulenameExist ?
                ValidationState.INVALID : ValidationState.VALID;
        mutation.getResult().setSchematronValidationState(schematronValidationState);

        final ExpectedResult schematronExp = mutation.getConfiguration().getSchematronExpectations().stream().findFirst().get().getExpectedResult();
        if (schematronExp.equals(ExpectedResult.PASS) && schematronValidationState.equals(ValidationState.VALID)
        || schematronExp.equals(ExpectedResult.FAIL) && schematronValidationState.equals(ValidationState.INVALID)) {
            mutation.getResult().setSchematronGlobalValidationAsExpected(true);
        } else {
            mutation.getResult().setSchematronGlobalValidationAsExpected(false);
        }
    }

    private boolean checkPresenceOfFailedRules(final SchematronOutput out, final List<String> ruleNamesDeclared) {
        final List<String> ruleNamesFailed = out.getFailedAsserts()
                .stream().map(FailedAssert::getId).collect(Collectors.toList());
        return CollectionUtils.containsAny(ruleNamesDeclared, ruleNamesFailed);
    }

    private void schemaValidation(final Mutation mutation) {
        if (this.schema != null) {
            try {
                final Document document = ObjectFactory.createDocumentBuilder(false)
                        .parse(this.targetFolder.resolve(mutation.getResultDocument()).toFile());
                final Result<Boolean, SyntaxError> result = Services.getSchemaValidatonService()
                        .validate(this.schema, document);
                mutation.addSchemaErrorMessages(result.getErrors());
                log.debug("Schema valid={}", result.isValid());
                mutation.getResult()
                        .setSchemaValidation(result.isValid() ? ValidationState.VALID : ValidationState.INVALID);
            } catch (final SAXException  e) {
                mutation.setState(State.ERROR);
                mutation.getGlobalErrorMessages().add("Invalid xml mutation produced");
            } catch (final IOException e) {
                mutation.setState(State.ERROR);
                mutation.getGlobalErrorMessages().add("Error while while trying to read the xml mutation file");
            }
        }

    }
}
