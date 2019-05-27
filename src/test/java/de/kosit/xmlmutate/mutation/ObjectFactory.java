package de.kosit.xmlmutate.mutation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * @author Andreas Penski
 */
public class ObjectFactory {

    public static MutationContext createContext(final String piString) {
        final Document doc = de.init.kosit.commons.ObjectFactory.createDocumentBuilder(false).newDocument();
        final Element root = doc.createElement("root");
        doc.appendChild(root);
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", piString);
        root.appendChild(pi);
        root.appendChild(doc.createElement("target"));
        return new MutationContext(pi, "test");
    }

}
