package de.kosit.xmlmutate.mutation;

import com.google.common.base.Charsets;
import de.init.kosit.commons.ObjectFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * It tests the parser functionalities
 * 
 * @author Andreas Penski
 */
public class MutationContextTest {

    @Test
    @DisplayName("Test Simple Context")
    public void testSimple() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            root.appendChild(pi);
            root.appendChild(inner);
        });
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getNodeName()).isEqualTo("inner");
    }

    @Test
    @DisplayName("Test Root Context")
    public void testRootContext() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            root.getParentNode().insertBefore(pi, root);
            root.appendChild(inner);
        });
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getNodeName()).isEqualTo("root");
    }

    @Test
    @DisplayName("Test no following sibling")
    public void testNoFollowingSibling() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            root.appendChild(inner);
            root.appendChild(pi);
        });
        assertThat(context.getTarget()).isNull();
    }

    @Test
    @DisplayName("Test whitespace between")
    public void testWhitespaceBetween() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            root.appendChild(pi);
            root.appendChild(doc.createTextNode("\t\n\n\n\t   "));
            root.appendChild(doc.createTextNode("\t\n\n\n\t   "));
            root.appendChild(inner);
        });
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getNodeName()).isEqualTo("inner");
    }

    @Test
    @DisplayName("Test comments between")
    public void testCommentsBetween() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            root.appendChild(pi);
            root.appendChild(doc.createTextNode("\t\n\n\n\t   "));
            root.appendChild(doc.createComment("\tsome comment\n\t\n\t"));
            root.appendChild(inner);
        });
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getNodeName()).isEqualTo("inner");
    }

    @Test
    @DisplayName("Test specific Target")
    public void testSetSpecificTarget() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            root.appendChild(pi);
            root.appendChild(inner);
        });
        final Element specific = context.getDocument().createElement("specific");
        context.getPi().getNextSibling().appendChild(specific);
        context.setSpecificTarget(specific);
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getNodeName()).isEqualTo("specific");
    }

    @Test
    @DisplayName("Test get parent ")
    public void testGetParent() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            root.appendChild(pi);
            root.appendChild(inner);
        });
        assertThat(context.getParentElement()).isNotNull();
        assertThat(context.getParentElement().getNodeName()).isEqualTo("root");
    }

    @Test
    @DisplayName("Test get parent of root ")
    public void testGetParentRoot() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            doc.insertBefore(pi, root);
            root.appendChild(inner);
        });
        assertThat(context.getParentElement()).isNull();
    }

    @Test
    @DisplayName("Test get parent deeply ")
    public void testGetParentDeep() {
        final MutationContext context = createContext((root, pi) -> {
            final Document doc = root.getOwnerDocument();
            final Element inner = doc.createElement("inner");
            root.appendChild(doc.createComment("c"));
            root.appendChild(doc.createTextNode("c"));
            root.appendChild(doc.createProcessingInstruction("c", "Data"));
            root.appendChild(doc.createProcessingInstruction("c", "Data"));
            root.appendChild(doc.createTextNode("\n\n\n"));
            root.appendChild(pi);
            root.appendChild(doc.createProcessingInstruction("c", "Data"));
            root.appendChild(inner);
        });
        assertThat(context.getParentElement()).isNotNull();
        assertThat(context.getParentElement().getNodeName()).isEqualTo("root");
        assertThat(context.getTarget().getNodeName()).isEqualTo("inner");
    }

    @Test
    public void testNullInitialisation() {
        assertThrows(NullPointerException.class, () -> {
            new MutationContext(null, Paths.get("dummy.xml"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            final Document doc = ObjectFactory.createDocumentBuilder(false).newDocument();
            new MutationContext(doc.createProcessingInstruction("test", "test"), null);
        });
    }

    private MutationContext createContext(final BiConsumer<Element, ProcessingInstruction> consumer) {
        return createContext("remove schema-valid", consumer);
    }

    private MutationContext createContext(final String piString, final BiConsumer<Element, ProcessingInstruction> consumer) {
        final Document doc = ObjectFactory.createDocumentBuilder(false).newDocument();
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", piString);
        final Element root = doc.createElement("root");
        doc.appendChild(root);
        consumer.accept(root, pi);
        return new MutationContext(pi, Paths.get("dummy.xml"));
    }

    private static String serialize(final Document doc) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Transformer transformer = ObjectFactory.createTransformer(true);
        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, Charsets.UTF_8)));
        out.close();
        return new String(out.toByteArray());
    }

}
