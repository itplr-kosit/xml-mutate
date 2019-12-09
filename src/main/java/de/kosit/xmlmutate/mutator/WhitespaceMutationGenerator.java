package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class WhitespaceMutationGenerator implements MutationGenerator {

    private static final String PROP_OPTIONS = "options";

    private static final String WHITESPACE = " ";

    // platform independent line break (for java would be \r\n)
    private static final String CARRIAGE_RETURN = System.lineSeparator();

    private static final String SEPARATOR = ",";

    @Override
    public List<String> getNames() {
        return Collections.singletonList(WhitespaceMutator.MUTATOR_NAME);
    }

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().orElseThrow(IllegalStateException::new);
    }

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {

        final List<Mutation> list = new ArrayList<>();

        // if only some variations shall be performed, check existence of PROP_OPTIONS
        if (config.getProperties().get(PROP_OPTIONS) != null) {
            list.addAll(config.resolveList(PROP_OPTIONS).stream().flatMap(e -> Arrays.stream(e.toString().split(SEPARATOR))
                    .filter(StringUtils::isNotEmpty).map(s -> createMutation(config, context, Variation.getVariationWithOrder(Integer.parseInt(s)))))
                    .collect(Collectors.toList()));
        } else {
            // Default all variations are performed
            Arrays.stream(Variation.values()).forEach(e -> list.add(createMutation(config, context, e)));
        }
        return list;
    }


    private Mutation createMutation(final MutationConfig config, final MutationContext context, final Variation variation) {
        final Mutator mutator = MutatorRegistry.getInstance().getMutator(getPreferredName());
        final MutationConfig cloned = config.cloneConfig();
        final String newText = getNewContent(variation, context.getTarget().getTextContent());
        cloned.add(WhitespaceMutator.INTERNAL_PROP_OPTION, newText);
        return new Mutation(context.cloneContext(),
                Services.getNameGenerator().generateName(context.getDocumentName(), variation.name()), cloned, mutator);
    }

    private String getNewContent(final Variation variation, final String textContent) {
        switch (variation) {
            case WHITESPACE_PREFIX:
                // Size is total length of final string
                return StringUtils.leftPad(textContent, textContent.length() + 1, WHITESPACE);
            case WHITESPACE_SUFFIX:
                return StringUtils.rightPad(textContent, textContent.length() + 1, WHITESPACE);
            case WHITESPACE_REPLACEMENT:
                return WHITESPACE;
            case CARRIAGE_RETURN_PREFIX:
                return CARRIAGE_RETURN + textContent;
            case CARRIAGE_RETURN_SUFFIX:
                return textContent + CARRIAGE_RETURN;
            case CARRIAGE_RETURN_REPLACEMENT:
                return CARRIAGE_RETURN;
            default:
                throw new IllegalArgumentException("Whitespace mutator variation unknown");
        }
    }

    @RequiredArgsConstructor
    public enum Variation {
        WHITESPACE_PREFIX(1),
        WHITESPACE_SUFFIX(2),
        WHITESPACE_REPLACEMENT(3),
        CARRIAGE_RETURN_PREFIX(4),
        CARRIAGE_RETURN_SUFFIX(5),
        CARRIAGE_RETURN_REPLACEMENT(6);

        @Getter
        private final int order;

        public static Variation getVariationWithOrder(final int order) {
            for (Variation e : Variation.values()) {
                if (order == e.order) return e;
            }
            throw new MutationException(ErrorCode.WHITESPACE_VARIATION_UNKNOWN, order);
        }

    }

}
