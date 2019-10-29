package de.kosit.xmlmutate.runner;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.Result;
import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutation.Schematron;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
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

    private final List<Schematron> schematronRules;

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

        for (final Schematron s : getSchematronRules()) {

            unknownRulenameExist = mutation.getConfiguration().getSchematronExpectations().stream()
                    .anyMatch(n -> !s.hasRule(n.getRuleName()));

            this.log.debug("Using schematron=" + s.getName());
            final SchematronOutput out = Services.getSchematronService()
                    .validate(s.getUri(), mutation.getContext().getDocument());
            this.log.debug("result=" + out.getText());
            failedAssertCount += out.getFailedAsserts().size();
            this.log.debug("failed asserts=" + failedAssertCount);
            mutation.getResult().addSchematronResult(s, out);

            List<String> ruleNamesDeclared = mutation.getConfiguration().getSchematronExpectations().stream().map(e -> e.getRuleName()).collect(Collectors.toList());
            List<String> ruleNamesFailed = out.getFailedAsserts().stream().map(n -> n.getId()).collect(Collectors.toList());
            if (CollectionUtils.containsAny(ruleNamesDeclared, ruleNamesFailed)) {
                failedRulesAreListed = true;
            }

        }

        mutation.getResult()
                .setSchematronValidation(failedRulesAreListed || unknownRulenameExist ? ValidationState.INVALID : ValidationState.VALID);

    }

    private void schemaValidation(final Mutation mutation) {
        if (this.schema != null) {
            try {
                final Document document = ObjectFactory.createDocumentBuilder(false)
                        .parse(this.targetFolder.resolve(mutation.getResultDocument()).toFile());
                final Result<Boolean, SyntaxError> result = Services.getSchemaValidatonService()
                        .validate(this.schema, document);
                mutation.getResult().getSchemaValidationErrors().addAll(result.getErrors());
                log.debug("Schema valid={}", result.isValid());
                mutation.getResult()
                        .setSchemaValidation(result.isValid() ? ValidationState.VALID : ValidationState.INVALID);
            } catch (final SAXException  e) {
                mutation.setState(State.ERROR);
                mutation.setErrorMessage("Invalid xml mutation produced");
            } catch (final IOException e) {
                mutation.setState(State.ERROR);
                mutation.setErrorMessage("Error while while trying to read the xml mutation file");
            }
        }

    }
}
