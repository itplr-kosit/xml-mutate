package de.kosit.xmlmutate.mutator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;

/**
 * Erzeugt Mutationen für eine definierte Liste mit Code-Werten. Die Liste kann eine simple
 * 
 * @author Andreas Penski
 */
public class CodeMutationGenerator implements MutationGenerator {

    private static final String PROP_VALUES = "values";

    private static final String PROP_GENERICODE = "genericode";

    private static final String SEPERATOR = ",";

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {
        final List<Mutation> list = new ArrayList<>();
        if (config.getProperties().get(PROP_VALUES) != null) {
            list.addAll(generateSimpleCodes(config, context));
        }
        if (config.getProperties().get(PROP_GENERICODE) != null) {
            list.addAll(generateGenericodeCodes(config, context));
        }
        if (list.isEmpty()) {
            throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, "No code source defined");
        }
        return list;
    }

    private Collection<Mutation> generateGenericodeCodes(final MutationConfig config, final MutationContext context) {
        return Collections.emptyList();
    }

    private Collection<Mutation> generateSimpleCodes(final MutationConfig config, final MutationContext context) {
        return config.resolveList(PROP_VALUES).stream()
                .flatMap(e -> Arrays.stream(e.toString().split(SEPERATOR)).filter(StringUtils::isNotEmpty).map(s -> {
                    final MutationConfig cloned = config.cloneConfig();
                    cloned.add(CodeMutator.INTERNAL_PROP_VALUE, s);
                    final Mutation m = new Mutation(context.cloneContext(),
                            Services.getNameGenerator().generateName(context.getDocumentName(), s.trim()));
                    m.setConfiguration(cloned);
                    m.setMutator(MutatorRegistry.getInstance().getMutator(getName()));
                    return m;

                })).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return CodeMutator.NAME;
    }
}
