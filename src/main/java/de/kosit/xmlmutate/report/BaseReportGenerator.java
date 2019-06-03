package de.kosit.xmlmutate.report;

import java.util.List;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * @author Andreas Penski
 */

public abstract class BaseReportGenerator implements ReportGenerator {

    /**
     * Zählt die Validen Mutationen innerhalb einer Liste mit Mutationen.
     * 
     * @param mutations die Mutationen.
     * @return Anzahl valider
     */
    protected static long aggregateValid(final List<Mutation> mutations) {
        return mutations.stream().filter(Mutation::isValid).count();
    }

    protected static long aggregateInvalid(final List<Mutation> mutations) {
        return mutations.stream().filter(Mutation::isInvalid).count();
    }
}
