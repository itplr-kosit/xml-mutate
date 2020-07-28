package de.kosit.xmlmutate.mutator;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

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

    private static Map<String, Mutator> initMutators() {
        final Map<String, Mutator> map = new HashMap<>();
        final Set<Class<? extends Mutator>> mutatorClasses = new Reflections("de").getSubTypesOf(Mutator.class);
        mutatorClasses.forEach(c -> {
            try {
                if (!Modifier.isAbstract(c.getModifiers())) {
                    final Mutator mutator = c.getDeclaredConstructor().newInstance();
                    mutator.getNames().forEach(o -> map.put(o, mutator));
                }
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Can not initialize mutators", e);
            }
        });
        return map;
    }

    private static Map<String, MutationGenerator> initGenerators() {
        final Map<String, MutationGenerator> map = new HashMap<>();
        final Set<Class<? extends MutationGenerator>> mutatorClasses = new Reflections("de").getSubTypesOf(MutationGenerator.class);
        mutatorClasses.forEach(c -> {
            try {
                final MutationGenerator mutator = c.getDeclaredConstructor().newInstance();
                mutator.getNames().forEach(o -> map.put(o, mutator));
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Can not initialize mutators", e);
            }
        });
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
