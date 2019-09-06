package de.kosit.xmlmutate.mutator;

import java.util.List;

import de.kosit.xmlmutate.mutation.Mutant;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.parser.MutatorInstruction;

/**
 * Zentrales Interface für
 *
 * @author Andreas Penski
 * @param <Mutation>
 */
public interface Mutator {

    String getName();

    void mutate(MutationContext context, MutationConfig config);

    List<Mutant> mutate(MutatorInstruction instruction);

}
