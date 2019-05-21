package de.kosit.xmlmutate.mutation;

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * Default Generator. Greift immer dann, wenn für den Mutator kein besondere Generator benötigt wird.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class DefaultMutationGenerator implements MutationGenerator {

    public static final String NAME = DefaultMutationGenerator.class.getSimpleName();

    private final NameGenerator nameGenerator;

    @Override
    public List<Mutation> generateMutations(final MutationConfig config, final MutationContext context) {
        final Mutation m = new Mutation(context, this.nameGenerator.generateName());
        m.setConfiguration(config);
        m.setMutator(MutatorRegistry.getMutator(config.getMutatorName()));
        return Collections.singletonList(m);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
