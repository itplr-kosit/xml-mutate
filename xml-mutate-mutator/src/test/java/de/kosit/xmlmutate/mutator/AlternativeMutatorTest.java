package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createRootCommentContext;
import static org.assertj.core.api.Assertions.assertThat;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;

/**
 * Tests for {@link AlternativeMutator}.
 *
 * @author Andreas Penski
 */
class AlternativeMutatorTest {


    private final AlternativeMutator mutator = new AlternativeMutator();

    @Test
    void testSimple() {
        MutationDocumentContext context = createRootCommentContext("mutator=alternative", node -> {
            Comment comment = node.getOwnerDocument().createComment("<some>some text</some>");
            node.appendChild(comment);
        });
        final MutationConfig config = createConfig().add(AlternativeMutator.ALT_KEY, 0);

        mutator.mutate(context, config);

        assertThat(context.getTarget().getPreviousSibling()).isNotNull();
        assertThat(context.getTarget().getPreviousSibling().getNodeType()).isEqualTo(Node.PROCESSING_INSTRUCTION_NODE);
        assertThat(context.getTarget().getPreviousSibling().getTextContent()).isEqualTo("mutator=alternative");

        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getLocalName()).isEqualTo("some");
        assertThat(context.getTarget().getChildNodes().getLength()).isEqualTo(1);
        assertThat(context.getTarget().getChildNodes().item(0).getNodeType()).isEqualTo(Node.TEXT_NODE);
        assertThat(context.getTarget().getChildNodes().item(0).getNodeValue()).isEqualTo("some text");

        assertThat(context.getTarget().getNextSibling()).isNotNull();
        assertThat(context.getTarget().getNextSibling().getNodeType()).isEqualTo(Node.COMMENT_NODE);
        assertThat(context.getTarget().getNextSibling().getNodeValue()).isEqualTo("<some>some text</some>");
    }

    @Test
    void testSingleCommentWitMultipleNodes() {
        final MutationDocumentContext context = createRootCommentContext("mutator=alternative", target -> {
            final Comment sub = target.getOwnerDocument().createComment("<some>xml</some><with>2nodes</with>");
            target.appendChild(sub);
        });
        final MutationConfig config = createConfig().add(AlternativeMutator.ALT_KEY, 0);

        mutator.mutate(context, config);

        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getLocalName()).isEqualTo("some");
        assertThat(context.getTarget().getFirstChild().getNodeValue()).isEqualTo("xml");
        assertThat(context.getTarget().getNextSibling().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getNextSibling().getLocalName()).isEqualTo("with");
        assertThat(context.getTarget().getNextSibling().getFirstChild().getNodeValue()).isEqualTo("2nodes");
    }

    @Test
    void testMultipleComments() {
        MutationDocumentContext context = createMutationContextWithTwoComments();
        System.out.println(TestHelper.serialize(context.getDocument()));

        mutator.mutate(context, createConfig().add(AlternativeMutator.ALT_KEY, 0));

        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getLocalName()).isEqualTo("some1");
        assertThat(context.getTarget().getFirstChild().getNodeValue()).isEqualTo("xml1");
        assertThat(context.getTarget().getNextSibling().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getNextSibling().getLocalName()).isEqualTo("with");
        assertThat(context.getTarget().getNextSibling().getFirstChild().getNodeValue()).isEqualTo("2nodes");


        context = createMutationContextWithTwoComments();
        mutator.mutate(context, createConfig().add(AlternativeMutator.ALT_KEY, 1));

        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getLocalName()).isEqualTo("some2");
        assertThat(context.getTarget().getFirstChild().getNodeValue()).isEqualTo("xml2");
    }

    private MutationDocumentContext createMutationContextWithTwoComments() {
        return createRootCommentContext("mutator=alternative", target -> {
            final Comment comment1 = target.getOwnerDocument().createComment("<some1>xml1</some1><with>2nodes</with>");
            final Comment comment2 = target.getOwnerDocument().createComment("<some2>xml2</some2>");
            target.appendChild(comment1);
            target.appendChild(comment2);
        });
    }

}
