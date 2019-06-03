package de.kosit.xmlmutate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Assertion für {@link Node}, inklusive {@link org.w3c.dom.Element}, {@link org.w3c.dom.Attr} und
 * {@link org.w3c.dom.Document}.
 *
 * @author Andreas Penski
 */
@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class NodeAssert extends AbstractAssert<NodeAssert, Node> {

    /**
     * Constructor.
     *
     * @param node der Knoten der getestet werden soll
     */
    NodeAssert(final Node node) {
        super(node, NodeAssert.class);
    }

    /**
     * Sichert, dass das Document einen definierten Namespace deklariert hat.
     *
     * @param namespace der gesuchte Namespace
     * @return this
     */
    public NodeAssert hasDeclaredNamespace(final String namespace) {
        isNotNull();
        if (!getDeclaredNamespaces().values().contains(namespace)) {
            failWithMessage("Expected namespace declaration %s namespace not found in the document", namespace);
        }
        return this;
    }

    private Map<String, String> getDeclaredNamespaces() {
        final Map<String, String> map = new HashMap<>();
        final NamedNodeMap attributes = getRootElement().getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                final Node node = attributes.item(i);
                if (node.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")) {
                    map.put(node.getNodeName(), node.getNodeValue());
                }
            }
        }
        return map;

    }

    /**
     * Stellt sicher, dass dieser Knoten ein Attribut ist.
     *
     * @return this
     */
    public NodeAssert isAttribute() {
        if (!Attr.class.isAssignableFrom(this.actual.getClass())) {
            failWithMessage("Expected an attribute. Found %s", this.actual.getClass());
        }
        return this;
    }

    /**
     * Stellt sicher, dass dieser Knoten ein {@link Element} ist.
     *
     * @return this
     */
    public NodeAssert isElement() {
        if (!Element.class.isAssignableFrom(this.actual.getClass())) {
            failWithMessage("Expected an element. Found %s", this.actual.getClass());
        }
        return this;
    }

    /**
     * Stellt sicher, dass dieser Knoten ein {@link Document} ist.
     *
     * @return this
     */
    public NodeAssert isDocument() {
        if (!_isDocument()) {
            failWithMessage("Expected a document. Found %s", this.actual.getClass());
        }
        return this;
    }

    private boolean _isDocument() {
        return Document.class.isAssignableFrom(this.actual.getClass());
    }

    /**
     * Stellt sicher, dass dieser Knoten das Wurzel-Element ist.
     *
     * @return this
     */
    public NodeAssert isDocumentElement() {
        isElement();
        if (!this.actual.getOwnerDocument().getDocumentElement().equals(this.actual)) {
            failWithMessage("Expected node to be the document element, but wasn't");
        }
        return this;
    }

    private Element getRootElement() {
        return getDocument().getDocumentElement();
    }

    private Document getDocument() {
        isNotNull();
        return _isDocument() ? (Document) this.actual : this.actual.getOwnerDocument();
    }

    /**
     * Sichert, dass dieser {@link Node} zu einem {@link Document} gehört, der einen definierten Root-Knoten hat.
     *
     * @param name der Names des Root-Knotens
     * @return this
     */
    public NodeAssert hasRootNode(final String name) {
        isNotNull();
        if (!Objects.equals(getRootElement().getNodeName(), name)) {
            failWithMessage("Expected root node <%s> not found. Was <%s>", name, getRootElement().getNodeName());
        }

        return this;
    }

    public NodeAssert hasAttribute(final String name) {
        isElement();
        if (((Element) this.actual).getAttributeNode(name) == null) {
            failWithMessage("Node has no attribute named %s, but is expected", name);
        }
        return this;
    }

    public NodeAssert hasTextContent(final String text) {
        isNotNull();
        if (!Objects.equals(this.actual.getTextContent(), text)) {
            failWithMessage("Node has text content '%s', expected is '%s'", this.actual.getTextContent(), text);
        }
        return this;
    }
}
