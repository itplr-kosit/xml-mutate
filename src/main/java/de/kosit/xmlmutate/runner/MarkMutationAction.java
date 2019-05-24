package de.kosit.xmlmutate.runner;

import java.text.MessageFormat;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * 
 * Actions fürs das Markieren der aktiven PI im Zieldokument.
 * 
 * @author Andreas Penski
 */
class MarkMutationAction {

    static class InsertCommentAction implements RunAction {

        @Override
        public void run(final Mutation mutation) {
            final Comment comment = mutation.getContext().getDocument()
                    .createComment(MessageFormat.format(" This is the active mutation configuration: {0} ", mutation.getIdentifier()));
            final Text textNode = mutation.getContext().getDocument().createTextNode("\n");
            mutation.getContext().getParentElement().insertBefore(comment, mutation.getContext().getTarget());
            mutation.getContext().getParentElement().insertBefore(textNode, mutation.getContext().getTarget());
        }

    }

    static class RemoveCommentAction implements RunAction {

        @Override
        public void run(final Mutation mutation) {

            final Element parent = mutation.getContext().getParentElement();
            final Node lf = mutation.getContext().getPi().getNextSibling();
            parent.removeChild(lf);
            final Node comment = mutation.getContext().getPi().getNextSibling();
            parent.removeChild(comment);

        }
    }
}
