package de.kosit.xmlmutate.mutator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mutator
 * @author Renzo Kottmann
 */
public class EmptyMutator implements Mutator {

    private final static Logger log = LogManager.getLogger(Mutator.class);
    private final static String MUTATOR_NAME = "empty";
    MutatorConfig config = null;

    EmptyMutator(MutatorConfig config) {
        this.config = config;
    }

    public String getName() {
        return EmptyMutator.MUTATOR_NAME;
    }
}