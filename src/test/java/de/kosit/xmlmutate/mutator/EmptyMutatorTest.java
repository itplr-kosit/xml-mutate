package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.TestHelper.serialize;
import static de.kosit.xmlmutate.TestHelper.stream;
import static de.kosit.xmlmutate.TestHelper.streamElements;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Testet den Empty Mutator
 * 
 * @author Andreas Penski
 */
public class EmptyMutatorTest {

    private final EmptyMutator mutator = new EmptyMutator();

    @Test
    public void testEmptyTextContent() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        this.mutator.mutate(context, createConfig());
        assertThat(context.getTarget().getTextContent()).isEmpty();
    }

    @Test
    public void testEmptyElement() {
        final MutationContext context = createContext(target -> {
            final Element sub = target.getOwnerDocument().createElement("sub");
            target.appendChild(sub);
        });
        this.mutator.mutate(context, createConfig());
        System.out.println(serialize(context.getDocument()));
        assertThat(context.getTarget().getTextContent()).isEmpty();
        assertThat(streamElements(context.getTarget().getChildNodes())).hasSize(0);
        assertThat(stream(context.getTarget().getChildNodes(), Node.COMMENT_NODE)).hasSize(1);
    }

    @Test
    public void testEmptyHierarchy() {
        final MutationContext context = createContext(target -> {
            final Element sub = target.getOwnerDocument().createElement("sub");
            final Element subsub = target.getOwnerDocument().createElement("subsub");
            sub.appendChild(subsub);
            target.appendChild(sub);
        });
        this.mutator.mutate(context, createConfig());

        assertThat(context.getTarget().getTextContent()).isEmpty();
        assertThat(streamElements(context.getTarget().getChildNodes())).hasSize(0);
        assertThat(stream(context.getTarget().getChildNodes(), Node.COMMENT_NODE)).hasSize(1);
    }

    @Test
    public void testEmptyAttribute() {
        final MutationContext context = createContext(target -> {
            target.setAttribute("test", "test");
        });
        this.mutator.mutate(context, createConfig().add("attribute", "test"));

        assertThat(context.getTarget().getAttributes().getNamedItem("test").getNodeValue()).isEmpty();
    }

    @Test
    public void testEmptyUnexistingAttribute() {
        final MutationContext context = createContext();
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig().add("attribute", "test"));
        });

    }

    @Test
    void testEmtpyTarget() {
        final MutationContext context = createContext(target -> {
        });
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig());
        });
    }
}
