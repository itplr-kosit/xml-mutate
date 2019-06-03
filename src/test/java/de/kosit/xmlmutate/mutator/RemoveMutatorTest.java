package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Testet {@link RemoveMutator}.
 * 
 * @author Andreas Penski
 */
public class RemoveMutatorTest {

    private final RemoveMutator mutator = new RemoveMutator();

    @Test
    public void simpleRemove() {
        final MutationContext context = createContext();
        final Node origTarget = context.getTarget();
        this.mutator.mutate(context, createConfig());
        assertThat(context.getTarget().getNodeType()).isEqualTo(Node.COMMENT_NODE);
        assertThat(origTarget.getParentNode()).isNull();
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
    public void testRemoveWrongElement() {
        final MutationContext context = createContext();
        final Element wrongElement = ObjectFactory.createDocumentBuilder(false).newDocument().createElement("someOtherElement");
        context.setSpecificTarget(wrongElement);
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

        final MutationConfig config = createConfig().add("attribute", "attr").add("attribute", "attr2").add("attribute", "attr3");
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

}
