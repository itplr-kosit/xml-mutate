package de.kosit.xmlmutate;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Some helper function for testing.
 * 
 * @author Andreas Penski
 */
public class TestHelper {

    public static final String SRC_TEST_RESOURCES_STR = "src/test/resources/";

    public static final URI SRC_TEST_RESOURCES = URI.create(SRC_TEST_RESOURCES_STR);

    public static final URI TEST_ROOT = Paths.get(SRC_TEST_RESOURCES_STR).toUri();

    private static final Consumer<Element> NOOP = whatever -> {
    };

    /**
     * Creates a simple mutation context for testing
     * 
     * @return the context
     */
    public static MutationContext createContext() {
        return createContext(NOOP);
    }


    /**
     * Create a mutation context with a give document for testing
     * @param doc
     * @return
     */
    public static MutationContext createContext(final Document doc, final String documentName) {
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", "mutator=noop");
        return new MutationContext(pi, documentName);
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param consumer a consumer for manipulating the target element
     * @return the context
     */
    public static MutationContext createContext(final Consumer<Element> consumer) {
        return createContext("mutator=noop", consumer);
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param piString a configuration for the PI
     * @return the context
     */
    public static MutationContext createContext(final String piString) {
        return createContext(piString, d -> {
        });
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param piString a configuration for the PI
     * @param consumer a consumer for manipulating the target element
     * @return the context
     */
    public static MutationContext createContext(final String piString, final Consumer<Element> consumer) {
        final Document doc = de.init.kosit.commons.ObjectFactory.createDocumentBuilder(false).newDocument();
        final Element root = doc.createElement("root");
        doc.appendChild(root);
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", piString);
        root.appendChild(pi);
        final Element target = doc.createElement("target");
        root.appendChild(target);
        consumer.accept(target);
        return new MutationContext(pi, "test");
    }

    /**
     * Creates a context with PI on root node.
     * 
     * @return the context
     */
    public static MutationContext createRootContext() {
        return createRootContext("mutator=noop", NOOP);
    }

    /**
     * Creates a context with PI on root node.
     * 
     * @param piString the pi string
     * @return the context
     */
    public static MutationContext createRootContext(final String piString) {
        return createRootContext(piString, NOOP);
    }

    /**
     * Creates a context with PI on root node.
     * 
     * @param piString the pi string
     * @param consumer consumer for manipulating the target node
     * @return the context
     */
    public static MutationContext createRootContext(final String piString, final Consumer<Element> consumer) {
        final Document doc = de.init.kosit.commons.ObjectFactory.createDocumentBuilder(false).newDocument();
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", piString);
        doc.appendChild(pi);
        final Element root = doc.createElement("root");
        doc.appendChild(root);
        consumer.accept(root);
        return new MutationContext(pi, "test");
    }

    /**
     * Create an empty {@link MutationConfig}.
     * 
     * @return the config
     */
    public static MutationConfig createConfig() {
        return new MutationConfig();
    }

    /**
     * Create a {@link MutationConfig} with some initial properties.
     * 
     * @return the config
     */
    public static MutationConfig createConfig(final Map<String, Object> properties) {
        final MutationConfig config = createConfig();
        config.setProperties(properties);
        return config;
    }

    public static String serialize(final Document doc) {
        try ( final StringWriter writer = new StringWriter() ) {
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (final IOException | TransformerException e) {
            throw new IllegalStateException("Can not serialize document", e);
        }
    }

    public static Stream<Node> streamElements(final NodeList list) {
        return stream(list, Node.ELEMENT_NODE);
    }

    public static Stream<Node> stream(final NodeList list, final short... types) {
        return IntStream.range(0, list.getLength()).mapToObj(list::item).filter(n -> ArrayUtils.contains(types, n.getNodeType()));
    }

}
