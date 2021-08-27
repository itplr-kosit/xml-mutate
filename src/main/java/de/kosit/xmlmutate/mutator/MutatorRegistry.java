package de.kosit.xmlmutate.mutator;

import java.util.HashMap;
import java.util.Map;

import de.kosit.xmlmutate.mutation.MutationGenerator;

/**
 * Registry for {@link Mutator} instance.
 *
 * @author Andreas Penski
 */
public class MutatorRegistry {

    /**
     * Internal structure to lazy-initialize the singleton.
     */
    private static class RegistryHolder {

        private static final MutatorRegistry REGISTRY;

        static {
            REGISTRY = new MutatorRegistry();
            REGISTRY.initialize();
        }

    }

    private Map<String, Mutator> mutators;

    private Map<String, MutationGenerator> generators;

    private MutatorRegistry() {
        // hide, singleton!
    }

    private void initialize() {
        this.mutators = initMutators();
        this.generators = initGenerators();
    }

    private static void registerMutator (final Mutator mutator, final Map<String, Mutator> map) {
      for (final String name : mutator.getNames ())
        map.put (name, mutator);
    }

    private static Map<String, Mutator> initMutators() {
        final Map<String, Mutator> map = new HashMap<>();
        registerMutator (new AlternativeMutator (), map);
        registerMutator (new CodeMutator (), map);
        registerMutator (new EmptyMutator (), map);
        registerMutator (new RemoveMutator (), map);
        registerMutator (new TextMutator (), map);
        registerMutator (new TransformationMutator (), map);
        registerMutator (new WhitespaceMutator (), map);
        registerMutator (new IdentityMutator (), map);
        return map;
    }

    private static void registerGenerator (final MutationGenerator gen, final Map<String, MutationGenerator> map) {
      for (final String name : gen.getNames ())
        map.put (name, gen);
    }

    private static Map<String, MutationGenerator> initGenerators() {
        final Map<String, MutationGenerator> map = new HashMap<>();
        registerGenerator (new AlternativeMutator (), map);
        registerGenerator (new CodeMutationGenerator (), map);
        registerGenerator (new DefaultMutationGenerator (), map);
        registerGenerator (new TextMutator (), map);
        registerGenerator (new TransformationMutationGenerator (), map);
        registerGenerator (new WhitespaceMutationGenerator (), map);
        return map;
    }

    /**
     * Returns a {@link Mutator} instance identified by name.
     *
     * @param name the name of the mutator
     * @return the mutator or null if no such mutator exists
     */
    public Mutator getMutator(final String name) {
        return this.mutators.get(name);
    }

    /**
     * Returns a {@link MutationGenerator} instance identified by name.
     *
     * @param name the name of the generator
     * @return the mutator or null if no such mutator exists
     */
    public MutationGenerator getGenerator(final String name) {
        final MutationGenerator mutationGenerator = this.generators.get(name);
        return mutationGenerator != null ? mutationGenerator : this.generators.get(DefaultMutationGenerator.NAME);

    }

    /**
     * Singleton-Access.
     *
     * @return die Registry
     */
    public static MutatorRegistry getInstance() {
        return RegistryHolder.REGISTRY;
    }
}
