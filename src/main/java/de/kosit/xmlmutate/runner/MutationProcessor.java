package de.kosit.xmlmutate.runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.Mutation;

/**
 * MutationProcessor
 */
public class MutationProcessor {

    public static void mutateDocument(Document d, Mutation mutation, Element target) {

    }

    public static void serialize(Document d, Mutation mutation, Path targetFolder) {
        try {
            final Path target = targetFolder.resolve(mutation.getResultDocument());
            Files.createDirectories(target.getParent());
            final OutputStream out = Files.newOutputStream(target);
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(
                    new DOMSource(mutation.getContext().getDocument()),
                    new StreamResult(new OutputStreamWriter(out, "UTF-8")));
            out.close();
        } catch (final TransformerException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
