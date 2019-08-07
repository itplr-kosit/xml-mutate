package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.Assertions.assertThat;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.TestHelper.createRootContext;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.runner.MarkMutationAction.InsertCommentAction;
import de.kosit.xmlmutate.runner.MarkMutationAction.RemoveCommentAction;

/**
 * Tests the marking and unmarking of current PIs.
 * 
 * @author Andreas Penski
 */
public class MarkMutationActionTest {

    private final InsertCommentAction insert = new InsertCommentAction();

    private final RemoveCommentAction remove = new RemoveCommentAction();

    @Test
    public void testSimpleMark() {
        final Mutation mutation = new Mutation(createContext(), RandomStringUtils.randomAlphanumeric(5));
        this.insert.run(mutation);
        assertThat(mutation.getContext().getPi().getPreviousSibling().getNodeType()).isEqualTo(Node.TEXT_NODE);
        assertThat(mutation.getContext().getPi().getPreviousSibling().getPreviousSibling().getNodeType()).isEqualTo(Node.COMMENT_NODE);
        assertThat(mutation.getContext().getPi().getPreviousSibling().getPreviousSibling().getTextContent()).contains("active");
    }

    @Test
    public void testSimpleUnmark() {
        final Mutation mutation = new Mutation(createContext(), RandomStringUtils.randomAlphanumeric(5));
        this.insert.run(mutation);
        assertThat(mutation.getContext().getParentElement().getFirstChild()).isNotSameNode(mutation.getContext().getPi());
        this.remove.run(mutation);
        assertThat(mutation.getContext().getParentElement().getFirstChild()).isSameNode(mutation.getContext().getPi());
    }

    @Test
    public void testSimpleMarkRoot() {
        final Mutation mutation = new Mutation(createRootContext(), RandomStringUtils.randomAlphanumeric(5));
        this.insert.run(mutation);
        assertThat(mutation.getContext().getPi().getPreviousSibling().getNodeType()).isEqualTo(Node.COMMENT_NODE);
        assertThat(mutation.getContext().getPi().getPreviousSibling().getTextContent()).contains("active");
    }

    @Test
    public void testSimpleUnmarkRoot() {
        final Mutation mutation = new Mutation(createRootContext(), RandomStringUtils.randomAlphanumeric(5));
        this.insert.run(mutation);
        assertThat(mutation.getContext().getDocument().getFirstChild()).isNotSameNode(mutation.getContext().getPi());
        this.remove.run(mutation);
        assertThat(mutation.getContext().getDocument().getFirstChild()).isSameNode(mutation.getContext().getPi());
    }
}
