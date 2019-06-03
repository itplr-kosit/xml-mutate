package de.kosit.xmlmutate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * @author Andreas Penski
 */
public class TestHelper {

    private static final Consumer<Element> NOOP = whatever -> {
    };

    public static MutationContext createContext() {
        return createContext(NOOP);
    }

    public static MutationContext createContext(final Consumer<Element> consumer) {
        return createContext("mutator=remove", consumer);
    }

    public static MutationContext createContext(final String piString) {
        return createContext(piString, d -> {
        });
    }

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

    public static MutationConfig createConfig() {
        return new MutationConfig();
    }

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
