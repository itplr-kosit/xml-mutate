package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.mutation.ObjectFactory.createContext;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * @author Andreas Penski
 */
public class ResetActionTest {

    private final ResetAction action = new ResetAction();

    @Test
    public void testSimpleReset() {
        final Mutation mutation = new Mutation(createContext("mutator=remove"), RandomStringUtils.randomAlphanumeric(5));
        final String nodeName = mutation.getContext().getTarget().getNodeName();
        mutation.getContext().getParentElement().replaceChild(mutation.getContext().getDocument().createElement("newElement"),
                mutation.getContext().getTarget());
        this.action.run(mutation);
        assertThat(mutation.getContext().getTarget()).isNotNull();
        assertThat(mutation.getContext().getTarget().getNodeName()).isEqualTo(nodeName);
    }
}
