package de.kosit.xmlmutate.mutator;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * Dieser Mutator entfernt das Ziel-Element aus dem Dokument bzw. ersetz es durch einen Kommentar.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class RemoveMutator extends BaseMutator {

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
        log.debug("Element to remove" + context);
        final Document doc = context.getDocument();
        final Comment remark = wrap(doc.createComment("Removed node "), context.getTarget());
        final Node parent = context.getParentElement();
        log.debug("Parent of context is=" + parent);
        final Node replaced = parent.replaceChild(remark, context.getTarget());
        log.debug("replaced node=" + replaced);
        context.setTarget(remark);
    }

}
