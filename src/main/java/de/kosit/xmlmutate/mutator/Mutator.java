package de.kosit.xmlmutate.mutator;

import java.util.List;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * A mutator mutates a document based on the provided configuration. Implementation of this interface are used by the
 * {@link de.kosit.xmlmutate.runner.MutationRunner}.
 * 
 * @author Andreas Penski
 */
public interface Mutator {

    /**
     * Returns a collection of names.
     * 
     * @return the name and aliasses of the mutator
     */
    List<String> getNames();

    /**
     * The preferred name of the mutator.
     * 
     * @return the preferred name
     */
    String getPreferredName();

    /**
     * Run the actual mutation.
     * 
     * @param context the context of the mutation e.g. target element
     * @param config the configuration to use
     */
    void mutate(MutationContext context, MutationConfig config);

}
