// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.runner.MutationException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This mutator deletes the target element from the document and replaces it with a comment.
 * This mutator can also delete atrtibutes.
 *
 * @author Andreas Penski
 */
public class RemoveMutator extends BaseMutator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RemoveMutator.class);

    @Override
    public List<String> getNames() {
        return Collections.singletonList("remove");
    }

    @Override
    public void mutate(final MutationDocumentContext context, final MutationConfig config) {
        if (context.getTarget() == null) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "No target found to remove");
        }
        final List<Object> atts = config.resolveList("attribute");
        if (CollectionUtils.isNotEmpty(atts)) {
            removeAttributes(context, atts);
        } else {
            removeElement(context);
        }
    }

    private void removeAttributes(final MutationDocumentContext context, final List<Object> atts) {
        final Node target = context.getTarget();
        atts.forEach(a -> {
            final Node attr = target.getAttributes().getNamedItem(a.toString());
            if (attr == null) {
                throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, String.format("Expected attribute %s not existing", a));
            }
            target.getAttributes().removeNamedItem(a.toString());
        });
    }

    private void removeElement(final MutationDocumentContext context) {
        if (context.getTarget().isSameNode(context.getDocument().getDocumentElement())) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Can not remove root element");
        }
        final Document doc = context.getDocument();
        final Comment remark = wrap(doc.createComment("Removed node "), context.getTarget());
        log.debug("Parent of context is {}", context.getParentElement());
        log.debug("Removing node {}", context.getTarget().getNodeName());
        context.getParentElement().replaceChild(remark, context.getTarget());
        context.setSpecificTarget(remark);
    }
}
