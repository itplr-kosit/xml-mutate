package de.kosit.xmlmutate.mutation;

import java.util.List;

/**
 * Generiert eine oder mehrere Mutationen aus der Konfiguration her.
 * 
 * @author Andreas Penski
 */
public interface MutationGenerator {

    List<Mutation> generateMutations(MutationConfig config, MutationContext context);

    String getName();

}
