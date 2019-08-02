package de.kosit.xmlmutate.mutator;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Dieser Mutator entfernt das Ziel-Element aus  dem Dokument bzw. ersetz es durch einen Kommentar. Weiterhin kann
 * der Mutator auch Attribute entfernen.
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
        if (context.getTarget() == null || !context.getParentElement().isSameNode(context.getTarget().getParentNode())) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "No target found to remove");
        }
        final List<Object> atts = config.resolveList("attribute");
        if (CollectionUtils.isNotEmpty(atts)) {
            removeAttributes(context, atts);
        } else {
            removeElement(context);
        }

    }

    private void removeAttributes(final MutationContext context, final List<Object> atts) {
        final Node target = context.getTarget();
        atts.forEach(a -> {
            final Node attr = target.getAttributes().getNamedItem(a.toString());
            if (attr == null) {
                throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, String.format("Expected attribute %s not existing", a));
            }
            target.getAttributes().removeNamedItem(a.toString());

        });
    }

    private void removeElement(final MutationContext context) {
        final Document doc = context.getDocument();
        final Comment remark = wrap(doc.createComment("Removed node "), context.getTarget());
        log.debug("Parent of context is=" + context.getParentElement());

        log.debug("Removing node {}", context.getTarget().getNodeName());
        context.getParentElement().replaceChild(remark, context.getTarget());
        context.setSpecificTarget(remark);
    }

}
