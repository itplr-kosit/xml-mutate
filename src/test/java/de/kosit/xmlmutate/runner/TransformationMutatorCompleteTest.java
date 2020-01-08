package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.TestResource.asPath;
import static de.kosit.xmlmutate.assertions.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.TestResource;
import de.kosit.xmlmutate.TestResource.TransformResource;

/**
 * Tests various inputs for {@link de.kosit.xmlmutate.mutator.TransformationMutator}.
 * 
 * @author Andreas Penski
 */
public class TransformationMutatorCompleteTest extends CompleteRun {

    /**
     * An implementation for traversing nodes using xpath and other access functionality mainly for testing documents
     * 
     */
    @RequiredArgsConstructor
    @Getter
    private static class NodeWithContext {

        private static final XPathFactory factory = XPathFactory.newInstance();

        private final Node node;

        public Stream<NodeWithContext> streamNodes(final String xpath) throws XPathExpressionException {
            return streamContextual(evaluateNodes(xpath));
        }

        public List<NodeWithContext> nodes(final String xpath) throws XPathExpressionException {
            return streamNodes(xpath).collect(Collectors.toList());
        }

        private Stream<NodeWithContext> streamContextual(final NodeList evaluateNodes) {
            return stream(evaluateNodes, Node.ELEMENT_NODE).map(NodeWithContext::wrap);
        }

        private static NodeWithContext wrap(final Node node) {
            return new NodeWithContext(node);
        }

        public NodeWithContext print() {
            try ( final StringWriter writer = new StringWriter() ) {
                final Transformer transformer = ObjectFactory.createTransformer(true);
                transformer.transform(new DOMSource(this.node), new StreamResult(writer));
                writer.flush();
                System.out.println(writer.toString());
            } catch (final IOException | TransformerException e) {
                throw new IllegalStateException("Can not serialize document", e);
            }
            return this;
        }

        private static Stream<Node> streamElements(final NodeList list) {
            return stream(list, Node.ELEMENT_NODE);
        }

        private static Stream<Node> stream(final NodeList list, final short... types) {

            return IntStream.range(0, list.getLength()).mapToObj(list::item)
                    .filter(n -> types.length == 0 || ArrayUtils.contains(types, n.getNodeType()));
        }

        private NodeList evaluateNodes(final String expression) throws XPathExpressionException {
            final XPath xpath = factory.newXPath();
            final XPathExpression compile = xpath.compile(expression);
            return (NodeList) compile.evaluate(this.node, XPathConstants.NODESET);
        }

        public NodeWithContext node(final String xpression) throws XPathExpressionException {
            return streamNodes(xpression).findFirst().orElse(null);
        }

        private short getNodeType() {
            return this.node.getNodeType();
        }

        public Stream<NodeWithContext> streamChildren() {
            return streamChildren(Node.ELEMENT_NODE);
        }

        public Stream<NodeWithContext> streamChildren(final short... nodeTypes) {
            return stream(this.node.getChildNodes(), nodeTypes).map(NodeWithContext::wrap);
        }

        public List<NodeWithContext> elementChildren() {
            return streamChildren().collect(Collectors.toList());
        }
    }

    @Test
    public void testSimple() throws XPathExpressionException {
        final RunnerConfig config = createConfig(TransformResource.BOOK_XML);
        config.addTemplate("simple", asPath(TransformResource.SIMPLE_TRANSFORM));
        final RunnerResult result = run(config);
        assertThat(result).hasMutationCount(1);
        final NodeWithContext document = parseResult(result.getResult().get(0).getValue().get(0).getResultDocument());
        Assertions.assertThat(document.nodes("//transformed")).hasSize(1);
        Assertions.assertThat(document.node("//transformed").elementChildren()).hasSize(4);
        assertThat(document.node("//transformed/doc").getNode()).hasChildren();
    }

    @Test
    public void testWithParameter() throws XPathExpressionException {
        final RunnerConfig config = createConfig(TransformResource.BOOK_XML_WITH_PARAM);
        config.addTemplate("simple", asPath(TransformResource.SIMPLE_TRANSFORM));
        final RunnerResult result = run(config);
        assertThat(result).hasMutationCount(1);
        final NodeWithContext document = parseResult(result.getResult().get(0).getValue().get(0).getResultDocument());
        Assertions.assertThat(document.nodes("//transformed")).hasSize(1);
        Assertions.assertThat(document.node("//transformed").elementChildren()).hasSize(4);
        assertThat(document.node("//transformed/param").getNode()).hasTextContent("should_be_transferred");
    }

    private NodeWithContext parseResult(final Path resultDocument) {
        return new NodeWithContext(DocumentParser.readDocument(Paths.get(TestResource.TEST_TARGET).resolve(resultDocument)));
    }

    @Test
    public void testMissingTemplate() {
        final RunnerConfig config = createConfig(TransformResource.BOOK_XML);
        final RunnerResult result = run(config);
        assertThat(result).isErroneous();
        assertThat(result).hasMutationCount(1);
        assertThat(result.getMutation(0)).containsError("Template \"simple\" not found");
    }

    @Test
    public void testInvalidTemplate() {
        final RunnerConfig config = createConfig(TransformResource.BOOK_XML);
        config.addTemplate("simple", asPath(TransformResource.INVALD_TRANSFORM));
        assertThrows(IllegalArgumentException.class, () -> run(config));
    }


}
