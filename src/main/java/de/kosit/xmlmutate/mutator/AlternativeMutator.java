package de.kosit.xmlmutate.mutator;

import static org.apache.commons.lang3.StringUtils.isNumeric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.DocumentParser;

/**
 * @author Andreas Penski
 */
public class AlternativeMutator extends BaseMutator implements MutationGenerator {

    static final String ALT_KEY = AlternativeMutator.class.getSimpleName() + "_" + "index";

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {
        final List<Mutation> l = new ArrayList<>();
        final long count = stream(context.getTarget().getChildNodes(), Node.COMMENT_NODE).count();
        IntStream.range(0, (int) count).forEach(c -> {
            final MutationConfig newConfig = config.cloneConfig().add(ALT_KEY, c);
            // final MutationContext context, final String identifier, MutationConfig
            // configuration
            final Mutation m = new Mutation(context.cloneContext(), Long.toString(c), newConfig);

            l.add(m);

        });
        return l;
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList("alternative");
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
        final List<Node> comments = stream(context.getTarget().getChildNodes(), Node.COMMENT_NODE)
                .collect(Collectors.toList());
        if (config.getProperties().get(ALT_KEY) == null || !isNumeric(config.getStringProperty(ALT_KEY))) {
            throw new IllegalArgumentException("No comment index configured");
        }
        final int index = Integer.parseInt(config.getStringProperty(ALT_KEY));
        if (index >= comments.size() || index < 0) {
            throw new IllegalArgumentException("No comment for index " + index);
        }
        final Node commentToUncomment = comments.get(index);
        final Document parsedFragment = DocumentParser
                .readDocument("<root>" + commentToUncomment.getTextContent() + "</root>");
        stream(parsedFragment.getDocumentElement().getChildNodes()).forEach(node -> {
            final Node newNode = context.getDocument().importNode(node, true);
            context.getTarget().insertBefore(newNode, commentToUncomment);
        });
        context.getTarget().removeChild(commentToUncomment);

    }
}
