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

import com.google.common.base.Charsets;

import lombok.RequiredArgsConstructor;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.Mutation;

/**
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class SerializeAction implements RunAction {

    private final Path targetFolder;

    @Override
    public void run(final Mutation mutation) {
        try {
            final Path target = this.targetFolder.resolve(mutation.getResultDocument());
            Files.createDirectories(target.getParent());
            final OutputStream out = Files.newOutputStream(target);
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(new DOMSource(mutation.getContext().getDocument()),
                    new StreamResult(new OutputStreamWriter(out, Charsets.UTF_8)));
            out.close();
        } catch (final TransformerException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
