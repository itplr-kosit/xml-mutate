package de.kosit.xmlmutate.mutation;

import de.kosit.xmlmutate.mutator.DefaultMutationGenerator;
import de.kosit.xmlmutate.mutator.Mutator;
import java.util.List;

/**
 * Generates one or mroe mutations out of a configuration. Generator and mutator form basically a single unit
 * and therefore every mutator should have its corresponding generator. A {@link DefaultMutationGenerator} is
 * available for simple mutators
 * <p>
 * Before the mutator do its processing, the generator generates suitable mutations, which prepares several variations
 * from the mutator configuration
 *
 * @author Andreas Penski
 */
public interface MutationGenerator {

    /**
     * The preferred generator name
     *
     * @return the generator name (identical to corresponding {@link Mutator#getPreferredName()})
     */
    String getPreferredName();

    /**
     * Generates one or more mutations
     *
     * @param config  the configuration from {@link org.w3c.dom.ProcessingInstruction}
     * @param context the {@link MutationDocumentContext} within a document
     * @return list with the mutations to be processed
     */
    List<Mutation> generateMutations(MutationConfig config, MutationDocumentContext context);

    /**
     * The generator names or the assignation to a concrete one
     *
     * @return the generator names (identical to corresponding {@link Mutator#getNames()})
     */
    List<String> getNames();

}
