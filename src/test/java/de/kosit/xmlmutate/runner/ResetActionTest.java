package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * Testet das Resetten des Dokuments.
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
}
