package de.kosit.xmlmutate.mutator;

import java.util.Collections;
import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.Services;

/**
 * Default Generator. Greift immer dann, wenn für den Mutator kein besonderer
 * Generator benötigt wird.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class DefaultMutationGenerator implements MutationGenerator {

    public static final String NAME = DefaultMutationGenerator.class.getSimpleName();

    @Override
    public List<Mutation> generateMutations(@NonNull final MutationConfig config,
            @NonNull final MutationContext context) {
        final Mutator mutator = Services.getRegistry().getMutator(config.getMutatorName());
        final Mutation m = new Mutation(context, Services.getNameGenerator().generateName(), config, mutator);

        return Collections.singletonList(m);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
