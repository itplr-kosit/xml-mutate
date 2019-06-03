package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * Eine Aktion des Runners. Kapselt eine bestimmte Funktion, welche auf Basis eine Mutation ausgeführt werden muss.
 *
 * @author Andreas Penski
 */
interface RunAction {

    /**
     * Führt die Aktion aus.
     * 
     * @param mutation die Mutation
     * @throws MutationException bei einem Verarbeitungsfehler
     */
    void run(Mutation mutation);
}
