package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import de.kosit.xmlmutate.runner.TemplateRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Generator for generating mutations for the {@link TransformationMutator}.
 *
 * @author Andreas Penski
 */
public class TransformationMutationGenerator implements MutationGenerator {

    private static final String PARAM_START = "param-";

    private static final String PROP_NAME = "template";

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
        final String templateConfig = config.getStringProperty(PROP_NAME);

        final Mutator mutator = Services.getRegistry().getMutator(config.getMutatorName());
        final String template = assertAndResolveName(templateConfig, context);
        if (!this.repository.exists(template)) {
            throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, MessageFormat.format("Template \"{0}\" not found", templateConfig));
        }

        config.add(TransformationMutator.TEMPLATE_PARAM, this.repository.getTemplate(template));
        config.add(TransformationMutator.PARAMETER_PARAM, collectParameters(config));
        final Mutation m = new Mutation(context, Services.getNameGenerator().generateName(context.getDocumentName(), mutator.getPreferredName(), null), config, mutator);

        return Collections.singletonList(m);
    }

    private String assertAndResolveName(final String template, final MutationContext context) {
        final String finalName;
        if (isNotBlank(template)) {
            if (this.repository.exists(template)) {
                finalName = template;
            } else {
                finalName = registerTemplateViaPath(template, context);
            }
        } else {
            finalName = null;
        }
        return finalName;
    }

    private String registerTemplateViaPath(final String template, final MutationContext context) {
        final String rel2doc = resolveRelative(template, context.getDocumentPath().getParent());
        return isNotBlank(rel2doc) ? rel2doc
                : resolveRelative(template, /** cwd */
                Paths.get(""));

    }

    private String resolveRelative(final String template, final Path path) {
        final Path resolved = path.resolve(template);
        if (Files.isRegularFile(resolved)) {
            if (this.repository.doesNotContain(resolved.toString())) {
                this.repository.registerTemplate(resolved.toString(), resolved);
            }
            return resolved.toString();
        }
        return null;
    }

    private Map<String, String> collectParameters(final MutationConfig config) {
        return config.getProperties().entrySet().stream().filter(e -> e.getKey().startsWith(PARAM_START))
                .collect(toMap(e -> e.getKey().substring(PARAM_START.length()), e -> e.getValue().toString()));
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("xslt");
    }

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().orElseThrow(IllegalStateException::new);
    }
}
