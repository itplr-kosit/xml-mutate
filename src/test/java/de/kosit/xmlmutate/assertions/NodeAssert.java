package de.kosit.xmlmutate.assertions;

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
 * Assertion for {@link Node}, including {@link org.w3c.dom.Element}, {@link org.w3c.dom.Attr} and
 * {@link org.w3c.dom.Document}.
 *
 * @author Andreas Penski
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class NodeAssert extends AbstractAssert<NodeAssert, Node> {

    /**
     * Constructor.
     *
     * @param node der node that should be tested
     */
    NodeAssert(final Node node) {
        super(node, NodeAssert.class);
    }

    /**
     * Ensures that the document has a defined namespace declared
     *
     * @param namespace the searched namespace
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
     * Ensures that this node is an attribute
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
     * Ensures that this node is an {@link Element}
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
     * Ensures that this node is a {@link Document}
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
     * Ensures that this node is a root element
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
     * Ensures that this {@link Node} belongs to a {@link Document}, which has a defined root node
     *
     * @param name the name of the root node
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

    public NodeAssert isSameNode(final Node other) {
        isNotNull();
        if (!this.actual.isSameNode(other)) {
            failWithMessage("Node %s is not the same node as %s ", this.actual.getNodeName(), other.getNodeName());
        }
        return this;
    }

    public NodeAssert isNotSameNode(final Node other) {
        isNotNull();
        if (this.actual.isSameNode(other)) {
            failWithMessage("Node %s is the same node as %s but should not ", this.actual.getNodeName(), other.getNodeName());
        }
        return this;
    }

    public NodeAssert hasNodeName(final String test) {
        isNotNull();
        if (!this.actual.getNodeName().equals(test)) {
            failWithMessage("Node name does not match. Expected %s. Actual is %s", test, this.actual.getNodeName());
        }
        return this;
    }

    public NodeAssert containsText(final String text) {
        isNotNull();
        if (!this.actual.getTextContent().contains(text)) {
            failWithMessage("Node should contain text '%s', but does not.", text);
        }
        return this;
    }

    public NodeAssert hasChildren() {
        isNotNull();
        if (this.actual.getChildNodes().getLength() == 0) {
            failWithMessage("Node should have children, but does not.");
        }
        return this;
    }
}
