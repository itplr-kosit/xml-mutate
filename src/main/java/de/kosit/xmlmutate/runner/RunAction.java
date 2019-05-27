package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * @author Andreas Penski
 */
interface RunAction {

    void run(Mutation mutation);
}
