package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Victor del Campo
 */

@Slf4j
public class RandomizeMutatorGenerator implements MutationGenerator {

    private static final String MAX_PARAM = "max";

    private static final int DEFAULT_MAX_POSSIBILITIES = 10;

    private static long MAX_PERMUTATIONS;

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().orElseThrow(IllegalStateException::new);
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList(RandomizeMutator.NAME);
    }

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {

        log.debug("Parent of context is=" + context.getParentElement());
        final Node targetNode = context.getTarget();
        log.debug("Permuting node {}", targetNode.getNodeName());
        final List<Node> childNodes = getChildNodes(targetNode);
        final long possiblePermutations = calculatePermutationsNumber(childNodes.size());
        log.debug("Possible permutations {}", possiblePermutations);
        log.debug("Max permutations {}", MAX_PERMUTATIONS);

        determineMaxPermutations(config, possiblePermutations);

        // Beginning with 1 since 1st (0) permutation is the one we already have in the original xml document
        // If the max possible permutations were set in the param of PI, do not +1
        final long finishPermutations = MAX_PERMUTATIONS == possiblePermutations ? MAX_PERMUTATIONS : MAX_PERMUTATIONS + 1;
        return LongStream.range(1, finishPermutations)
                .mapToObj(i -> createMutation(config, context, permutation(i, childNodes)))
                .collect(Collectors.toList());
    }

    private Mutation createMutation(final MutationConfig config, final MutationContext context, final List<Node> nodes) {
        final Mutator mutator = MutatorRegistry.getInstance().getMutator(getPreferredName());
        final MutationConfig cloned = config.cloneConfig();
        cloned.add(RandomizeMutator.INTERNAL_PROP_VALUE, nodes);
        return new Mutation(context.cloneContext(), Services.getNameGenerator().generateName(context.getDocumentName()), cloned,
                mutator);
    }

    private List<Node> getChildNodes(final Node nodeParent) {
        final List<Node> list = new ArrayList<>();
        final NodeList childNodes = nodeParent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                list.add(node);
            }
        }
        if (list.isEmpty() || list.size() == 1) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Target node has only 1 child element or none");
        }
        return list;
    }

    private static void determineMaxPermutations(final MutationConfig config, final long possiblePermutations) {
        if (config.getProperties().get(MAX_PARAM) != null) {
            preCheckMaxParam(config.resolveList(MAX_PARAM));
            final long maxParamPermutations = Long.parseLong(config.resolveList(MAX_PARAM).get(0).toString());
            MAX_PERMUTATIONS = Math.min(maxParamPermutations, possiblePermutations);
        } else {
            MAX_PERMUTATIONS = Math.min(DEFAULT_MAX_POSSIBILITIES, possiblePermutations);
        }
    }

    private static void preCheckMaxParam(final List<Object> objects) {
        if (objects.size() > 1) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Only 1 max parameter declaration allowed");
        } else {
            try {
                Integer.parseInt(objects.get(0).toString());
            } catch (final NumberFormatException e) {
                throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Only 1 integer max parameter value allowed");
            }
        }
    }

    private static long calculatePermutationsNumber(final int num) {
        return LongStream.rangeClosed(1, num).reduce(1, (x, y) -> x * y);
    }

    private static <T> List<T> permutationHelper(long no, LinkedList<T> in, List<T> out) {
        if (in.isEmpty()) return out;
        long subFactorial = calculatePermutationsNumber(in.size() - 1);
        out.add(in.remove((int) (no / subFactorial)));
        return permutationHelper(no % subFactorial, in, out);
    }

    private static <T> List<T> permutation(long no, List<T> items) {
        return permutationHelper(
                no,
                new LinkedList<>(Objects.requireNonNull(items)),
                new ArrayList<>()
        );
    }

}
