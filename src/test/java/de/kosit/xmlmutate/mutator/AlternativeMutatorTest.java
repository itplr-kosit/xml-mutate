package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;

/**
 * Tests for {@link AlternativeMutator}.
 *
 * @author Andreas Penski
 */
public class AlternativeMutatorTest {


    private final AlternativeMutator mutator = new AlternativeMutator();


    @Test
    public void testSimple() {
        final MutationDocumentContext context = createContext(target -> {
            final Comment sub = target.getOwnerDocument().createComment("<some>xml</some>");
            target.appendChild(sub);
        });
        final MutationConfig config = createConfig().add(AlternativeMutator.ALT_KEY, "0");
        mutator.mutate(context, config);
        assertThat(context.getTarget().getChildNodes().getLength()).isEqualTo(1);
        assertThat(context.getTarget().getChildNodes().item(0).getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getChildNodes().item(0).getNodeName()).isEqualTo("some");
    }

    @Test
    public void testMultipleNodes() {
        final MutationDocumentContext context = createContext(target -> {
            final Comment sub = target.getOwnerDocument().createComment("<some>xml</some><with>2nodes</with>");
            target.appendChild(sub);
        });
        final MutationConfig config = createConfig().add(AlternativeMutator.ALT_KEY, "0");
        mutator.mutate(context, config);
        assertThat(context.getTarget().getChildNodes().getLength()).isEqualTo(2);
        assertThat(context.getTarget().getChildNodes().item(0).getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getChildNodes().item(0).getNodeName()).isEqualTo("some");
        assertThat(context.getTarget().getChildNodes().item(1).getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getChildNodes().item(1).getNodeName()).isEqualTo("with");
    }

    @Test
    public void testMultipleComments() {
        final MutationDocumentContext context = createContext(target -> {
            final Comment comment1 = target.getOwnerDocument().createComment("<some>xml</some><with>2nodes</with>");
            final Comment comment2 = target.getOwnerDocument().createComment("<some>xml</some>");
            target.appendChild(comment1);
            target.appendChild(comment2);
        });
        System.out.println(TestHelper.serialize(context.getDocument()));
        final MutationConfig config = createConfig().add(AlternativeMutator.ALT_KEY, "0");
        mutator.mutate(context, config);
        assertThat(context.getTarget().getChildNodes().getLength()).isEqualTo(3);
        assertThat(context.getTarget().getChildNodes().item(0).getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getChildNodes().item(0).getNodeName()).isEqualTo("some");
        assertThat(context.getTarget().getChildNodes().item(1).getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(context.getTarget().getChildNodes().item(1).getNodeName()).isEqualTo("with");
        assertThat(context.getTarget().getChildNodes().item(2).getNodeType()).isEqualTo(Node.COMMENT_NODE);
    }

    @Test
    public void testWrongConfiguration() {
        final MutationDocumentContext context = createContext();
        final MutationConfig config = createConfig();
        Executable executable = () -> mutator.mutate(context, config);
        assertThrows(IllegalArgumentException.class, executable);

        config.add(AlternativeMutator.ALT_KEY, "0");
        assertThrows(IllegalArgumentException.class, executable);

        config.getProperties().clear();
        config.add(AlternativeMutator.ALT_KEY, "t");
        assertThrows(IllegalArgumentException.class, executable);

        config.getProperties().clear();
        context.getTarget().appendChild(context.getDocument().createComment("bla"));
        config.add(AlternativeMutator.ALT_KEY, "0");
        config.add(AlternativeMutator.ALT_KEY, "1");
        assertThrows(IllegalArgumentException.class, executable);
    }
}
