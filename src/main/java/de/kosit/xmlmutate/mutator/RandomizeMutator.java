package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Victor del Campo
 */
@Slf4j
public class RandomizeMutator extends BaseMutator {

    static final String NAME = "random-element-order";

    static final String INTERNAL_PROP_VALUE = RandomizeMutator.class.getSimpleName() + ".randomize";

    @Override
    public List<String> getNames() {
        return Collections.singletonList(NAME);
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {

        if (context.getTarget() == null) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "No target found to randomize");
        }

        final Node cloneTargetNode = context.getTarget().cloneNode(true);

        final List<Node> newNodeOrderList = config.resolveList(INTERNAL_PROP_VALUE).stream().map(m -> (Node) m).collect(Collectors.toList());

        log.debug("New order of childs {}", newNodeOrderList.stream().map(Node::getLocalName).collect(Collectors.joining(",")));

        final Node newTargetNode = rearrangeNode(cloneTargetNode, newNodeOrderList);

        context.getTarget().getParentNode().replaceChild(newTargetNode, context.getTarget());

        context.setSpecificTarget(newTargetNode);

    }

    private Node rearrangeNode(final Node clone, final List<Node> nodeListe) {
        final NodeList childNodes = clone.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                clone.replaceChild(nodeListe.get(0), node);
                nodeListe.remove(0);
            }
        }
        return clone;
    }


}
