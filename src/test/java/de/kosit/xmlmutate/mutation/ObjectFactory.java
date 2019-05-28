package de.kosit.xmlmutate.mutation;

import java.util.Map;
import java.util.function.Consumer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * @author Andreas Penski
 */
public class ObjectFactory {

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

}
