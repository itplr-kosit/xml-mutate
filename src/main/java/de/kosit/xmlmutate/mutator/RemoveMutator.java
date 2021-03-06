package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;

/**
 * This mutator deletes the target element from the document and replaces it with a comment.
 * This mutator can also delete atrtibutes.
 *
 * @author Andreas Penski
 */
@Slf4j
public class RemoveMutator extends BaseMutator {

    @Override
    public List<String> getNames() {
        return Arrays.asList("remove");
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
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
        if (context.getTarget().isSameNode(context.getDocument().getDocumentElement())) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Can not remove root element");
        }
        final Document doc = context.getDocument();
        final Comment remark = wrap(doc.createComment("Removed node "), context.getTarget());
        log.debug("Parent of context is=" + context.getParentElement());

        log.debug("Removing node {}", context.getTarget().getNodeName());
        context.getParentElement().replaceChild(remark, context.getTarget());
        context.setSpecificTarget(remark);
    }

}
