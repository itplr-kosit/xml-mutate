package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.Services;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class TextMutator extends BaseMutator implements MutationGenerator {

    private static final String NAME = "text";

    private static final String LENGTH_PARAM = "length";

    private static final String MAX_LENGTH_PARAM = "max-length";

    private static final String MIN_LENGTH_PARAM = "min-length";

    private static final int DEFAULT_MAX_LENGTH = 10000;

    private final TextGenerator textGenerator = new TextGenerator();


    @Override
    public List<String> getNames() {
        return Arrays.asList(NAME);
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
        final Node target = context.getTarget();
        target.setTextContent(
                this.textGenerator.generateAlphaNumeric((Integer) config.getProperties().get(LENGTH_PARAM)));
    }

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {
        final List<Mutation> l = new ArrayList<>();
        l.add(noMutation(config, context));
        l.addAll(maxLengthMutations(config, context));
        l.addAll(minLengthMutations(config, context));
        return l;
    }

    private Mutation noMutation(final MutationConfig config, final MutationContext context) {
        return create(config, context, context.getTarget().getTextContent().length());
    }

    private Collection<Mutation> maxLengthMutations(final MutationConfig config, final MutationContext context) {
        final List<Mutation> l = new ArrayList<>();
        final String maxLength = (String) config.getProperties().get(MAX_LENGTH_PARAM);
        final int length = StringUtils.isNoneBlank(maxLength) ? Integer.parseInt(maxLength) : DEFAULT_MAX_LENGTH;

        l.add(create(config.cloneConfig(), context.cloneContext(), length));
        l.add(create(config.cloneConfig(), context.cloneContext(), length - 1));
        final Mutation violation = create(config.cloneConfig(), context.cloneContext(), length + 1);
        violation.getConfiguration().setSchemaValidationExpectation(ExpectedResult.FAIL);
        l.add(violation);

        return l;
    }

    private Collection<Mutation> minLengthMutations(final MutationConfig config, final MutationContext context) {
        final List<Mutation> l = new ArrayList<>();
        final String minLength = (String) config.getProperties().get(MIN_LENGTH_PARAM);
        if (minLength != null) {
            l.add(create(config.cloneConfig(), context.cloneContext(), Integer.parseInt(minLength)));
            l.add(create(config.cloneConfig(), context.cloneContext(), Integer.parseInt(minLength) + 1));
            final Mutation violation = create(
                    config.cloneConfig(), context.cloneContext(), Integer.parseInt(minLength) - 1);
            violation.getConfiguration().setSchemaValidationExpectation(ExpectedResult.FAIL);
            l.add(violation);
        }
        return l;
    }

    private Mutation create(final MutationConfig cloneConfig, final MutationContext cloneContext, final int length) {
        final String postfix = "length-" + length;
        cloneConfig.getProperties().put(LENGTH_PARAM, length);

        return new Mutation(cloneContext, Services.getNameGenerator().generateName(cloneContext.getDocumentName(), NAME, postfix),
                cloneConfig, Services.getRegistry().getMutator(NAME));
    }
}
