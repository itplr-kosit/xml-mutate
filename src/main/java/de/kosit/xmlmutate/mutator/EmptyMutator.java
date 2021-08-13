// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mutator for emptying an element. If the element to be emptied contains any subelements, they will be removed.
 * If it contains text it will be removed.
 *
 * @author Renzo Kottmann
 * @author Andreas Penski
 */
public class EmptyMutator extends BaseMutator {
    @java.lang.SuppressWarnings("all")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmptyMutator.class);
    private static final String MUTATOR_NAME = "empty";

    @Override
    public List<String> getNames() {
        return Collections.singletonList(EmptyMutator.MUTATOR_NAME);
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
        log.debug("Element to make empty" + context);
        final Node target = context.getTarget();
        log.debug("First target of context is=" + target);
        final List<Object> atts = config.resolveList("attribute");
        if (CollectionUtils.isNotEmpty(atts)) {
            emptyAttributes(context, atts);
        } else if (streamElements(target.getChildNodes()).count() > 0) {
            emptyBody(context);
        } else {
            emptyText(context);
        }
    }

    private void emptyAttributes(final MutationContext context, final List<Object> atts) {
        final Node target = context.getTarget();
        atts.forEach(a -> {
            final Node attr = target.getAttributes().getNamedItem(a.toString());
            if (attr == null) {
                throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, String.format("Expected attribute %s not existing", a));
            }
            attr.setTextContent("");
        });
    }

    private void emptyText(final MutationContext context) {
        final Node target = context.getTarget();
        if (StringUtils.isBlank(target.getTextContent())) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Nothing found to empty");
        }
        final Comment comment = context.getDocument().createComment("Emptied: " + context.getTarget().getTextContent());
        target.setNodeValue(null);
        target.setTextContent(null);
        target.appendChild(comment);
    }

    private void emptyBody(final MutationContext context) {
        final Node target = context.getTarget();
        final List<Node> childs = streamElements(target.getChildNodes()).collect(Collectors.toList());
        final Comment comment = wrap(context.getDocument().createComment("Emptied: \n"), childs);
        childs.forEach(target::removeChild);
        target.appendChild(comment);
    }
}
