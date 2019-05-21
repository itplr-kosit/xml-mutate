package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * Zentrales Interface für
 * 
 * @author Andreas Penski
 */
public interface Mutator {

    String getName();

    void mutate(MutationContext context, MutationConfig config);

}
