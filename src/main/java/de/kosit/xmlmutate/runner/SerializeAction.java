package de.kosit.xmlmutate.runner;

import com.google.common.base.Charsets;
import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.Mutation;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serializes the mutated {@link org.w3c.dom.Document} into a file
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class SerializeAction implements RunAction {

    private final Path targetFolder;

    @Override
    public void run(final Mutation mutation) {
        final Path target = this.targetFolder.resolve(mutation.getResultDocument());
        serialize(mutation.getContext().getDocument(), target);
    }

    public static void serialize(final Document document, final Path target) {
        try {
            Files.createDirectories(target.toAbsolutePath().getParent());
            final OutputStream out = Files.newOutputStream(target);
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(new DOMSource(document),
                    new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
            out.close();
        } catch (final TransformerException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
