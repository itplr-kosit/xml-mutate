package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * Action for XMute Runner.
 *
 * @author Andreas Penski
 * @author Renzo Kottmann
 */
interface RunAction {

    /**
     * Executes the action.
     *
     * @param mutation
     *                     The Mutation
     * @throws MutationException
     *                               In case of run time errors of the
     *                               MutationRunner
     */
    void run(Mutation mutation);

}
