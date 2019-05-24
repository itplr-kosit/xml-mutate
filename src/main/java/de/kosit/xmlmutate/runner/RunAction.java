package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * @author Andreas Penski
 */
public interface RunAction {

    void run(Mutation mutation);
}
