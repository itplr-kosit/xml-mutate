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
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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

        for (final Schematron s : getSchematronRules()) {
            this.log.debug("Using schematron=" + s.getName());
            final SchematronOutput out = Services.getSchematronService()
                    .validate(s.getUri(), mutation.getContext().getDocument());
            this.log.debug("result=" + out.getText());
            failedAssertCount += out.getFailedAsserts().size();
            this.log.debug("failed asserts=" + failedAssertCount);
            mutation.getResult().addSchematronResult(s, out);
        }
        mutation.getResult()
                .setSchematronValidation(failedAssertCount > 0 ? ValidationState.INVALID : ValidationState.VALID);
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
            } catch (final SAXException | IOException e) {
                // Wenn ein XML nicht eingelesen werden kann, kommt eine SAXParseException
                if(e instanceof SAXParseException) {
                    mutation.setState(State.ERROR);
                    mutation.setErrorMessage("Invalid xml mutation produced");
                }
                e.printStackTrace();
            }
        }

    }
}
