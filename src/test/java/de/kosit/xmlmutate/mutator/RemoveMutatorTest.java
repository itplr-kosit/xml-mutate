package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.TestHelper.createInstruction;
import static de.kosit.xmlmutate.TestHelper.createRootContext;
// import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.util.List;

import javax.xml.transform.Source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.xmlunit.builder.Input;

import de.kosit.xmlmutate.cli.XmlMutateUtil;
import de.kosit.xmlmutate.mutation.Mutant;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.parser.MutatorInstruction;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Tests {@link RemoveMutator}.
 *
 * @author Renzo Kottmann
 * @author Andreas Penski
 */

public class RemoveMutatorTest {
    private static final Logger log = LoggerFactory.getLogger(RemoveMutatorTest.class);
    private final RemoveMutator mutator = new RemoveMutator();
    private final String SIMPLE_XML = "<root><?xmute mutator=\"remove\" ?><e></e></root>";

    @Test
    @DisplayName("Simple element remove test")
    @Tag("mutator")
    public void simpleRemove() {

        final List<MutatorInstruction> instruction = createInstruction(SIMPLE_XML);
        assertNotNull(instruction);
        assertEquals(instruction.size(), 1);

        List<Mutant> mutation = instruction.stream().findFirst().get().createMutants();
        assertAll(
                "Mutation list should exist and have size one", () -> assertNotNull(mutation, "Should exist"),
                () -> assertEquals(mutation.size(), 1, "Size one only")

        );
        Mutant mutant = mutation.get(0);
        log.trace("original frag={}", XmlMutateUtil.printToString(mutant.getOriginalFragment(), 2));

        final DocumentFragment frag = mutant.getMutatedFragment();
        assertNotNull(frag);

        log.trace("clone={}", XmlMutateUtil.printToString(mutant.getCloneFragment(), 2));
        log.trace("mutant={}", XmlMutateUtil.printToString(frag, 2));
        Source source = Input.fromNode(frag).build();
        assertThat(source).hasXPath("/");
        assertThat(source).doesNotHaveXPath("/root/e");
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
    @Tag("mutator")
    public void testRemoveAttribute() {
        final String REMOVE_ATTR = "<root><?xmute mutator=\"remove\" attribute=\"aa bb\" ?><e aa=\"have a\" bb=\"have b\"></e></root>";
        final List<MutatorInstruction> instruction = createInstruction(REMOVE_ATTR);
        assertNotNull(instruction);
        assertEquals(instruction.size(), 1);

        List<Mutant> mutation = instruction.stream().findFirst().get().createMutants();
        assertAll(
                "Mutation list should exist and have size one", () -> assertNotNull(mutation, "Should exist"),
                () -> assertEquals(mutation.size(), 1, "Size one only")

        );
        Mutant mutant = mutation.get(0);

        log.trace("original frag={}", XmlMutateUtil.printToString(mutant.getOriginalFragment(), 2));

        final DocumentFragment frag = mutant.getMutatedFragment();
        assertNotNull(frag);

        log.trace("mutant={}", XmlMutateUtil.printToString(frag, 2));
        Source source = Input.fromNode(frag).build();
        assertThat(source).hasXPath("/e");
        assertThat(source).doesNotHaveXPath("/e/@aa");
        assertThat(source).doesNotHaveXPath("/e/@aa");

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
