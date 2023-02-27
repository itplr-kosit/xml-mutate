package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.TestHelper.createRootContext;
import static org.assertj.core.api.Assertions.assertThat;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * It tests the reset action of a document.
 *
 * @author Andreas Penski
 */
public class ResetActionTest {

    private final ResetAction action = new ResetAction();

    @Test
    public void testSimpleReset() {
        final Mutation mutation = new Mutation(createContext(), RandomStringUtils.randomAlphanumeric(5));
        final String nodeName = mutation.getContext().getTarget().getNodeName();
        mutation.getContext().getParentElement().replaceChild(mutation.getContext().getDocument().createElement("newElement"),
                mutation.getContext().getTarget());
        this.action.run(mutation);
        assertThat(mutation.getContext().getTarget()).isNotNull();
        assertThat(mutation.getContext().getTarget().getNodeName()).isEqualTo(nodeName);
    }

    @Test
    public void testWrongTargetDocument() {
        final Mutation mutation = new Mutation(createContext(), RandomStringUtils.randomAlphanumeric(5));

        Assertions.assertThrows(MutationException.class, () -> {
            mutation.getContext().setSpecificTarget(
                    de.init.kosit.commons.ObjectFactory.createDocumentBuilder(false).newDocument().createElement("newNe"));
            this.action.run(mutation);
        });
    }

    @Test
    public void testOrphanTarget() {
        final Mutation mutation = new Mutation(createContext(), RandomStringUtils.randomAlphanumeric(5));

        Assertions.assertThrows(MutationException.class, () -> {
            final Node target = mutation.getContext().getTarget();
            final Node removed = mutation.getContext().getPi().getParentNode().removeChild(target);
            mutation.getContext().setSpecificTarget(removed);
            this.action.run(mutation);
        });
    }

    @Test
    public void testResetRootNode() {
        final Mutation mutation = new Mutation(createRootContext(), RandomStringUtils.randomAlphanumeric(5));
        final MutationDocumentContext ctx = mutation.getContext();
        ctx.getTarget().appendChild(ctx.getDocument().createElement("someNewElement"));
        ((Element) ctx.getTarget()).setAttribute("att", "value");
        this.action.run(mutation);
        assertThat(ctx.getTarget()).isNotNull();
        assertThat(ctx.getTarget().getChildNodes().getLength()).isEqualTo(0);
        assertThat(ctx.getTarget().getAttributes().getLength()).isEqualTo(0);
    }
}
