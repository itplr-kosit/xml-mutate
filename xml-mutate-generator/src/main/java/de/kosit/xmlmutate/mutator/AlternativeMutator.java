package de.kosit.xmlmutate.mutator;

import static org.apache.commons.lang3.StringUtils.isNumeric;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.DocumentParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.collections4.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This mutator operates on a set of comments, which are used as alternatives to the target element.
 *
 * @author Andreas Penski
 */
public class AlternativeMutator extends BaseMutator implements MutationGenerator {

    static final String ALT_KEY = AlternativeMutator.class.getSimpleName() + "_" + "index";

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationDocumentContext context) {
        final List<Mutation> l = new ArrayList<>();
        final long count = context.findTargets(Node.COMMENT_NODE).size();
        IntStream.range(0, (int) count).forEach(c -> {
            final MutationConfig newConfig = config.cloneConfig().add(ALT_KEY, c);
            final Mutation m = createMutation(newConfig, context, Long.toString(c));
            l.add(m);
        });
        return l;
    }

    private Mutation createMutation(final MutationConfig config, final MutationDocumentContext context, final String identifier) {
        final Mutator mutator = MutatorRegistry.getInstance().getMutator(getPreferredName());
        return new Mutation(context.cloneContext(), identifier, config, mutator);
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList("alternative");
    }

    @Override
    public void mutate(final MutationDocumentContext context, final MutationConfig config) {
        final List<Node> comments = context.findTargets(Node.COMMENT_NODE);
        if (config.getProperties().get(ALT_KEY) == null || !isNumeric(config.getStringProperty(ALT_KEY))) {
            throw new IllegalArgumentException("No comment index configured");
        }
        final int index = (Integer)config.getProperties().get(ALT_KEY);
        if (CollectionUtils.isEmpty(comments) || index >= comments.size() || index < 0) {
            throw new IllegalArgumentException("No comment for index " + index);
        }

        final Node elementToUncomment = comments.get(index);
        final String namespaces = retrieveNamespaces(context.getDocument().getDocumentElement().getAttributes());
        final Document wrappedUncommentedDoc = DocumentParser.readDocument("<root " + namespaces + " >" + elementToUncomment.getTextContent() + "</root>", true);
        final Node importedUncommentedElement = context.getDocument().importNode(wrappedUncommentedDoc.getDocumentElement(), true);

        List<Node> addedNodes = findNodesToBeInserted(importedUncommentedElement);
        addedNodes.forEach(node -> context.getParentElement().insertBefore(node, elementToUncomment));
        context.setMutatedTargets(addedNodes);
    }

    private List<Node> findNodesToBeInserted(Node importedUncommentedElement) {
        List<Node> addedNodes = new ArrayList<>();
        Node child = importedUncommentedElement.getFirstChild();
        addedNodes.add(child);
        for (Node n = child.getNextSibling(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                addedNodes.add(n);
            }
        }
        return addedNodes;
    }

    private String retrieveNamespaces(NamedNodeMap attributes) {
        return IntStream.range(0, attributes.getLength())
            .mapToObj(attributes::item)
            .map(Objects::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }

}
