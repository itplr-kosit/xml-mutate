package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Generator for Mutation having a defined vlaue from a list of values.
 *
 * @author Andreas Penski
 */
@Slf4j
public class CodeMutationGenerator implements MutationGenerator {

    private static final String PROP_VALUES = "values";

    private static final String PROP_TRIM = "trim";

    private static final boolean DEFAULT_TRIM = true;

    private static final String DEFAULT_SEPARATOR = ",";

    private static final String SEPARATOR = "separator";

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {
        final List<Mutation> list = new ArrayList<>();
        if (config.getProperties().get(PROP_VALUES) != null) {
            list.addAll(generateSimpleCodes(config, context));
        }
        if (list.isEmpty()) {
            throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, "No code source found");
        }
        return list;
    }


    private Collection<Mutation> generateSimpleCodes(final MutationConfig config, final MutationContext context) {
        final String separator = config.getProperties().get(SEPARATOR) != null ? config.getProperties().get(SEPARATOR).toString() : DEFAULT_SEPARATOR;
        return config.resolveList(PROP_VALUES).stream().flatMap(e -> Arrays.stream(e.toString().split(Pattern.quote(separator)))
                .filter(StringUtils::isNotEmpty).map(s -> createMutation(config, context, s))).collect(Collectors.toList());
    }

    private Mutation createMutation(final MutationConfig config, final MutationContext context, final String code) {
        final boolean doTrim = config.getProperties().get(PROP_TRIM) != null ? Boolean.parseBoolean(config.getProperties().get(PROP_TRIM).toString()) : DEFAULT_TRIM;

        final Mutator mutator = MutatorRegistry.getInstance().getMutator(getPreferredName());
        final MutationConfig cloned = config.cloneConfig();
        cloned.add(CodeMutator.INTERNAL_PROP_VALUE, doTrim ? trim(code) : code);
        return new Mutation(context.cloneContext(), Services.getNameGenerator().generateName(context.getDocumentName(), doTrim ? trim(code) : code), cloned,
                mutator);
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList(CodeMutator.NAME);
    }

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().orElseThrow(IllegalStateException::new);
    }
}
