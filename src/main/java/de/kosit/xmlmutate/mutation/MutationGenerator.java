package de.kosit.xmlmutate.mutation;

import java.util.List;

import de.kosit.xmlmutate.mutator.DefaultMutationGenerator;
import de.kosit.xmlmutate.mutator.Mutator;

/**
 * Generiert eine oder mehrere Mutationen aus der Konfiguration. Generator und Mutator bilden grundsätzlich eine Einheit
 * und es sollte zu jedem Mutator ein passender Generator existieren. Für einfache Mutatoren steht ein
 * {@link DefaultMutationGenerator} bereit.
 * 
 * Vor der Verarbeitung durch den Mutator, erzeugt der Generator passende Mutationen, welche mehrere Varianten der
 * Konfiguration für den Mutator vorbereitet.
 *
 * 
 * @author Andreas Penski
 */
public interface MutationGenerator {

    /**
     * Der präferierte Name des Generatores
     *
     * @return der Name des Generators (identisch mit passenden {@link Mutator#getPreferredName()})
     */
    String getPreferredName();

    /**
     * Generiert eine oder mehrere Mutationen.
     * 
     * @param config die Konfiguration aus der {@link org.w3c.dom.ProcessingInstruction}
     * @param context der {@link MutationContext} innerhalb des Dokuments
     * @return Liste mit zu verarbeitenden Mutationen
     */
    List<Mutation> generateMutations(MutationConfig config, MutationContext context);

    /**
     * Die Namen des Generatores bzw. die Zuordnung zu einem konkrente
     * 
     * @return die Namen des Generators (identisch mit passenden {@link Mutator#getNames()})
     */
    List<String> getNames();

}
