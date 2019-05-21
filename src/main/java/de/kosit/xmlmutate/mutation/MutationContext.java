package de.kosit.xmlmutate.mutation;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import lombok.Getter;
import lombok.Setter;

/**
 * Der Kontext der Mutation. Alle nötigen Artefakte um die Position innerhalb des Zieldokuments zu bestimmen.
 * 
 * @author Andreas Penski
 */
@Getter
@Setter
public class MutationContext {

    private final String documentName;

    private final ProcessingInstruction pi;

    private final DocumentFragment originalFragment;

    private Node target;

    public MutationContext(final ProcessingInstruction pi, final String name) {
        this.pi = pi;
        this.documentName = name;
        this.originalFragment = createFragment();
    }

    MutationContext(final ProcessingInstruction pi, final String name, final DocumentFragment fragment) {
        this.pi = pi;
        this.documentName = name;
        this.originalFragment = fragment;
    }

    public Node getTarget() {
        if (this.target == null) {
            return findTarget();
        }
        return this.target;
    }

    private Element findTarget() {
        Node sibling = this.pi;
        while ((sibling = sibling.getNextSibling()) != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) sibling;
            }
        }
        return null;
    }

    private DocumentFragment createFragment() {
        final DocumentFragment fragment = this.pi.getOwnerDocument().createDocumentFragment();
        final Node target = getTarget();
        if (target != null) {
            fragment.appendChild(target.cloneNode(true));
            return fragment;
        }
        return null;
    }

    public Document getDocument() {
        return this.pi.getOwnerDocument();
    }

    public Element getParentElement() {
        return (Element) getPi().getParentNode();
    }

    /**
     * Erstellt eine Kopie des Kontexts, wenn beispieleweise mehrere Mutationen aus einer PI erstellt werden müssen.
     * 
     * @return eine Kopie
     */
    public MutationContext cloneContext() {
        return new MutationContext(getPi(), getDocumentName());
    }
}
