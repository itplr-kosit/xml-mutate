package de.kosit.xmlmutate.mutator;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import de.kosit.xmlmutate.runner.TemplateRepository;

/**
 * Generator for generating mutations for the {@link TransformationMutator}.
 *
 * @author Andreas Penski
 */
public class TransformationMutationGenerator implements MutationGenerator {

    private static final String PARAM_START = "param-";

    private static final String PROP_NAME = "name";

    private final TemplateRepository repository;

    TransformationMutationGenerator(final TemplateRepository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("unused")
    public TransformationMutationGenerator() {
        this.repository = Services.getTemplateRepository();
    }

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {
        final String name = config.getStringProperty(PROP_NAME);
        final Mutator mutator = Services.getRegistry().getMutator(config.getMutatorName());
        if (isEmpty(name) || !this.repository.exists(name)) {
            throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, "Template {0} not found", name);
        }

        config.add(TransformationMutator.TEMPLATE_PARAM, this.repository.getTemplate(name));
        config.add(TransformationMutator.PARAMETER_PARAM, collectParameters(config));
        final Mutation m = new Mutation(context, Services.getNameGenerator().generateName(), config, mutator);

        return Collections.singletonList(m);
    }

    private Map<String, String> collectParameters(final MutationConfig config) {
        return config.getProperties().entrySet().stream().filter(e -> e.getKey().startsWith(PARAM_START))
                .collect(toMap(e -> e.getKey().substring(PARAM_START.length()), e -> e.getValue().toString()));
    }

    @Override
    public String getName() {
        return "xslt";
    }
}
