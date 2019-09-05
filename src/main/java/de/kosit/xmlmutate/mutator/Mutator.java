package de.kosit.xmlmutate.mutator;

import java.util.List;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * Zentrales Interface für
 * 
 * @author Andreas Penski
 */
public interface Mutator {

    List<String> getNames();

    String getPreferredName();

    void mutate(MutationContext context, MutationConfig config);

}
