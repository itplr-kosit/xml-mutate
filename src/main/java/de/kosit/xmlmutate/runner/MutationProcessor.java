package de.kosit.xmlmutate.runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.cli.XmlMutateUtil;
import de.kosit.xmlmutate.mutation.Mutant;

/**
 * MutationProcessor
 *
 * @author Renzo Kottmann
 */
public class MutationProcessor {
    private static final Logger log = LoggerFactory.getLogger(MutationProcessor.class);

    // apply mutation to original doc
    public static void mutateDocument(Document d, Mutant mutant, boolean comment) {
        final ProcessingInstruction pi = mutant.getPI();

        Element target = mutant.getTarget();
        Node parent = target.getParentNode();
        log.trace("mutated frag={}", XmlMutateUtil.printToString(mutant.getMutatedFragment()));

        if (parent != null) {
            parent.replaceChild(mutant.getMutatedFragment(), target);
            // todo check for root node behaviour
        }
        if (comment) {

        } else {
            // take care pi can be everywhere in doc if xpath is implemented
            parent = pi.getParentNode();
            parent.removeChild(pi);
        }
    }

    public static void serialize(Document d, Mutant mutant, Path targetFolder) {
        try {

            final Path target = targetFolder.resolve(mutant.getName() + ".xml");
            log.trace("Writing doc to {}", target.toString());

            Files.createDirectories(target.getParent());
            final OutputStream out = Files.newOutputStream(target);
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(new DOMSource(d), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
            out.close();
        } catch (final TransformerException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void validateSchema(final Document doc, Mutant mutant, Schema schema) {

        // final CollectingErrorEventHandler errorHandler = new
        // CollectingErrorEventHandler();
        try {
            final Validator validator = createValidator(schema);
            // validator.setErrorHandler(errorHandler);
            validator.validate(new DOMSource(doc));

            // final Result<Boolean, SyntaxError> result =

        } catch (final SAXException | IOException e) {

            throw new IllegalStateException(e);
        }

    }

    private static Validator createValidator(final Schema schema) {
        final Validator validator = schema.newValidator();
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (final SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");
            log.error(e.getMessage(), e);
        }
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (final SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");
            log.error(e.getMessage(), e);

        }

        return validator;
    }

}
