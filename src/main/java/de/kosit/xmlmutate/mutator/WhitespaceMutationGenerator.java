package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class WhitespaceMutationGenerator implements MutationGenerator {

    private static final String PROP_VARIATIONS = "variations";

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
        if (config.getProperties().get(PROP_VARIATIONS) != null) {
            list.addAll(config.resolveList(PROP_VARIATIONS).stream().flatMap(e -> Arrays.stream(e.toString().split(SEPARATOR))
                    .filter(StringUtils::isNotEmpty).map(s -> createMutation(config, context, WhitespaceMutator.Variation.getVariationWithOrder(Integer.parseInt(s)))))
                    .collect(Collectors.toList()));
        } else {
            // Default all variations are performed
            Arrays.stream(WhitespaceMutator.Variation.values()).forEach(e -> list.add(createMutation(config, context, e)));
        }
        if (list.isEmpty()) {
            throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, "No variations source found");
        }
        return list;
    }


    private Mutation createMutation(final MutationConfig config, final MutationContext context, final WhitespaceMutator.Variation variation) {
        final Mutator mutator = MutatorRegistry.getInstance().getMutator(getPreferredName());
        final MutationConfig cloned = config.cloneConfig();
        final String newText = variation.getNewTextContent(context.getTarget().getTextContent());
        cloned.add(WhitespaceMutator.INTERNAL_PROP_OPTION, newText);
        return new Mutation(context.cloneContext(),
                Services.getNameGenerator().generateName(context.getDocumentName(), variation.name()), cloned, mutator);
    }


}
