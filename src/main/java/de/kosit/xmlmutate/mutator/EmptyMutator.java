package de.kosit.xmlmutate.mutator;

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Comment;
import org.w3c.dom.Node;

import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * Mutator, der ein Element leert. Enthält es Unterelemente, werden diese entfernt. Enthält es Text, werden diese
 * entfernt.
 * 
 * @author Renzo Kottmann
 * @author Andreas Penski
 */
@Slf4j
public class EmptyMutator extends BaseMutator {

    private final static String MUTATOR_NAME = "empty";

    @Override
    public String getName() {
        return EmptyMutator.MUTATOR_NAME;
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
        log.debug("Element to make empty" + context);
        final Node target = context.getTarget();
        log.debug("First target of context is=" + target);
        final List<Node> childs = streamElements(target.getChildNodes()).collect(Collectors.toList());

        if (childs.size() > 0) {
            final Comment comment = wrap(context.getDocument().createComment("Emptied: \n"), childs);
            childs.forEach(target::removeChild);
            target.appendChild(comment);
        } else {
            final Comment comment = context.getDocument().createComment("Emptied: " + target.getNodeValue());
            target.setNodeValue(null);
            target.appendChild(comment);
        }

    }

}