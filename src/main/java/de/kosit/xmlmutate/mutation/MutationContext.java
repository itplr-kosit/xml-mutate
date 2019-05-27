package de.kosit.xmlmutate.mutation;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import lombok.Getter;
import lombok.NonNull;
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

    public MutationContext(@NonNull final ProcessingInstruction pi, @NonNull final String name) {
        if (pi == null || isBlank(name)) {
            throw new IllegalArgumentException("PI and name must be set");
        }
        this.pi = pi;
        this.documentName = name;
        this.originalFragment = createFragment();
    }

    public void setTarget(final Node element) {
        this.target = element;
    }

    public int getLevel() {
        int level = 0;
        Node current = this.pi;
        while ((current = current.getParentNode()) != null) {
            level++;
        }
        return level;
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
        final Node targetElement = getTarget();
        if (targetElement != null) {
            fragment.appendChild(targetElement.cloneNode(true));
            return fragment;
        }
        return null;
    }

    /**
     * Gibt das Document zurück, das hinter dem PI steht.
     * 
     * @return Document
     */
    public Document getDocument() {
        return this.pi.getOwnerDocument();
    }

    /**
     * Gibt das Parent Element des PI zurück. Ist das PI über vor dem Root-Element angesiedelt, so ist parent null
     * 
     * @return das parent Element
     */
    public Element getParentElement() {
        final Node parentNode = getPi().getParentNode();
        return parentNode instanceof Element ? (Element) parentNode : null;
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
