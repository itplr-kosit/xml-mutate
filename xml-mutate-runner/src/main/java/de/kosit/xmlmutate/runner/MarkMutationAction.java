package de.kosit.xmlmutate.runner;

import java.text.MessageFormat;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 *
 * Actions for marking and demarking active xmute Processing Instruction
 *
 * @author Andreas Penski
 */
class MarkMutationAction {

    private MarkMutationAction() {
        // hide
    }

    static class InsertCommentAction implements RunAction {

        @Override
        public void run(final Mutation mutation) {
            final Comment comment = mutation.getContext().getDocument().createComment(
                    MessageFormat.format("This is the active mutation configuration: {0} ", mutation.getIdentifier()));
            final Text textNode = mutation.getContext().getDocument().createTextNode("\n");
            if (mutation.getContext().getParentElement() != null) {
                mutation.getContext().getParentElement().insertBefore(comment, mutation.getContext().getPi());
                mutation.getContext().getParentElement().insertBefore(textNode, mutation.getContext().getPi());
            } else {
                mutation.getContext().getDocument().insertBefore(comment, mutation.getContext().getPi());
            }
        }

    }

    static class RemoveCommentAction implements RunAction {

        @Override
        public void run(final Mutation mutation) {

            final Element parent = mutation.getContext().getParentElement();
            final ProcessingInstruction pi = mutation.getContext().getPi();
            if (parent != null) {
                final Node lf = pi.getPreviousSibling();
                parent.removeChild(lf);
                final Node comment = pi.getPreviousSibling();
                parent.removeChild(comment);
            } else {
                // must be root node
                mutation.getContext().getDocument().removeChild(pi.getPreviousSibling());
            }

        }
    }
}
