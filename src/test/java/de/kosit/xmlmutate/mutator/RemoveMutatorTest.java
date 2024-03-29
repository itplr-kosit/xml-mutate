package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.TestHelper.createRootContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.runner.MutationException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

/**
 * It tests the {@link RemoveMutator}.
 * 
 * @author Andreas Penski
 */
public class RemoveMutatorTest {

    private final RemoveMutator mutator = new RemoveMutator();

    @Test
    public void simpleRemove() {
        final MutationDocumentContext context = createContext();
        final Node origTarget = context.getTarget();
        this.mutator.mutate(context, createConfig());
        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.COMMENT_NODE);
        assertThat(origTarget.getParentNode()).isNull();
    }

    @Test
    public void testUnexisting() {
        final MutationDocumentContext context = createContext();
        context.getParentElement().removeChild(context.getTarget());
        assertThrows(MutationException.class, () -> this.mutator.mutate(context, createConfig()));
    }

    @Test
    public void testRemoveAttribute() {
        final MutationDocumentContext context = createContext(target ->
            target.setAttribute("attr", "value"));

        this.mutator.mutate(context, createConfig().add("attribute", "attr"));
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getAttributes().getLength()).isZero();
    }

    @Test
    public void testRemoveMulipleAttributes() {
        final MutationDocumentContext context = createContext(target -> {
            target.setAttribute("attr", "value");
            target.setAttribute("attr2", "value2");
            target.setAttribute("attr3", "value3");
        });

        final MutationConfig config = createConfig().add("attribute", "attr").add("attribute", "attr2").add("attribute", "attr3");
        this.mutator.mutate(context, config);
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getAttributes().getLength()).isZero();
    }

    @Test
    public void testRemoveSpecificAttribute() {
        final MutationDocumentContext context = createContext(target -> {
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
        final MutationDocumentContext context = createContext(target ->
            target.setAttribute("attr", "value"));
        assertThrows(MutationException.class, () -> {
            final MutationConfig config = createConfig().add("attribute", "doesNotExist");
            this.mutator.mutate(context, config);
        });
    }

    @Test
    public void testRemoveRoot() {
        final MutationDocumentContext context = createRootContext();
        assertThrows(MutationException.class, () -> this.mutator.mutate(context, createConfig()));
    }

}
