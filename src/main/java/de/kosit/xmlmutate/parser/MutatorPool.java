package de.kosit.xmlmutate.parser;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import de.kosit.xmlmutate.mutator.Mutator;

/**
 * MutatorPool
 * 
 * @author Renzo Kottmann
 * @author Andreas Penski
 */
public class MutatorPool {

    private static Map<String, Mutator> mutators = new HashMap<String, Mutator>();;

    static {
        mutators = initMutators();
    }

    private static Map<String, Mutator> initMutators() {
        final Map<String, Mutator> map = new HashMap<>();
        final Set<Class<? extends Mutator>> mutatorClasses = new Reflections("de").getSubTypesOf(Mutator.class);
        mutatorClasses.forEach(c -> {
            try {
                if (!Modifier.isAbstract(c.getModifiers())) {
                    final Mutator mutator = c.getDeclaredConstructor().newInstance();
                    map.put(mutator.getName(), mutator);
                }

            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Can not initialize mutators", e);
            }
        });
        return map;
    }

    /**
     * Gets a Muator with given Name.
     *
     * @param name
     *                 der Name des Mutators
     * @return Mutator or null if there is no Mutator with given name.
     */
    public static Mutator getMutator(final String name) {
        return mutators.get(name);
    }

}
