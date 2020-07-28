package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.Services;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Default generator.  It is called when a mutator is not in need of an special generator
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class DefaultMutationGenerator implements MutationGenerator {

    public static final String NAME = DefaultMutationGenerator.class.getSimpleName();

    @Override
    public List<Mutation> generateMutations(@NonNull final MutationConfig config,
                                            @NonNull final MutationContext context) {
        // Get first name of name list of mutator

        final Mutator mutator = Services.getRegistry().getMutator(config.getMutatorName());
        final Mutation m = new Mutation(context, Services.getNameGenerator().generateName(context.getDocumentName(), mutator.getPreferredName(), null), config, mutator);


        return Collections.singletonList(m);
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList(NAME);
    }

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().orElseThrow(IllegalStateException::new);
    }
}
