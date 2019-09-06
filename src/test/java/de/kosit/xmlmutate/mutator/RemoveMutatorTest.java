package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.TestHelper.createRootContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.parser.MutatorInstruction;
import de.kosit.xmlmutate.runner.DocumentParser;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Tests {@link RemoveMutator}.
 *
 * @author Renzo Kottmann
 * @author Andreas Penski
 */
public class RemoveMutatorTest {

    private final RemoveMutator mutator = new RemoveMutator();
    private final String SIMPLE_XML = "<root><?xmute mutator=\"remove\" ?><e></e></root>";

    private List<MutatorInstruction> createInstruction(String xml) {

        Document doc = DocumentParser.readDocument(xml);
        return DocumentParser.parseMutatorInstruction(doc, "test");

    }

    @Test
    @DisplayName("Simple element remove test")
    @Tag("current")
    public void simpleRemove() {
        final List<MutatorInstruction> instruction = createInstruction(SIMPLE_XML);
        assertNotNull(instruction);
        assertEquals(instruction.size(), 1);

        List<Mutation> mutation = instruction.stream().findFirst().get().execute();
        assertAll(
                "Mutation list should exist and of size one", () -> assertNotNull(mutation, "Should exist"),
                () -> assertEquals(mutation.size(), 1, "Size one only")

        );

        // assertThat(context.getTarget().getNodeType()).isEqualTo(Node.COMMENT_NODE);
        // assertThat(origTarget.getParentNode()).isNull();
    }

    @Test
    public void testUnexisting() {
        final MutationContext context = createContext();
        context.getParentElement().removeChild(context.getTarget());
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig());
        });
    }

    @Test
    public void testRemoveAttribute() {
        final MutationContext context = createContext(target -> {
            target.setAttribute("attr", "value");
        });

        this.mutator.mutate(context, createConfig().add("attribute", "attr"));
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getAttributes().getLength()).isEqualTo(0);
    }

    @Test
    public void testRemoveMulipleAttributes() {
        final MutationContext context = createContext(target -> {
            target.setAttribute("attr", "value");
            target.setAttribute("attr2", "value2");
            target.setAttribute("attr3", "value3");
        });

        final MutationConfig config = createConfig().add("attribute", "attr").add("attribute", "attr2")
                .add("attribute", "attr3");
        this.mutator.mutate(context, config);
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getAttributes().getLength()).isEqualTo(0);
    }

    @Test
    public void testRemoveSpecificAttribute() {
        final MutationContext context = createContext(target -> {
            target.setAttribute("attr", "value");
            target.setAttribute("attr2", "value2");
            target.setAttribute("attr3", "value3");
        });

        final MutationConfig config = createConfig().add("attribute", "attr").add("attribute", "attr3");
        this.mutator.mutate(context, config);
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getAttributes().getLength()).isEqualTo(1);
        assertThat(context.getTarget().getAttributes().getNamedItem("attr2")).isNotNull();
    }

    @Test
    public void testRemoveUnexistingAttribute() {
        final MutationContext context = createContext(target -> {
            target.setAttribute("attr", "value");
        });
        assertThrows(MutationException.class, () -> {
            final MutationConfig config = createConfig().add("attribute", "doesNotExist");
            this.mutator.mutate(context, config);
        });
    }

    @Test
    public void testRemoveRoot() {
        final MutationContext context = createRootContext();
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig());
        });
    }

}
