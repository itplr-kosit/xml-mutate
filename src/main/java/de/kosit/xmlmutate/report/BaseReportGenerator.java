package de.kosit.xmlmutate.report;

import static java.lang.Math.toIntExact;

import java.util.List;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * @author Andreas Penski
 */

public abstract class BaseReportGenerator implements ReportGenerator {

    /**
     * Counts all valid mutations
     * 
     * @param mutations all mutations
     * @return number of valid mutations
     */
    protected static long aggregateValid(final List<Mutation> mutations) {
        return mutations.stream().filter(Mutation::isValid).count();
    }

    /**
     * Counts failed mutations
     * 
     * @param mutations all mutations
     * @return number of failed mutations
     */
    protected static long countFailures(final List<Mutation> mutations) {
        return mutations.stream().filter(Mutation::isInvalid).filter(m -> !m.isErroneous()).count();
    }

    /**
     * Counts mutations with processing errors.
     * 
     * @param mutations all mutations
     * @return number of mutations with errors
     */
    protected int countErrors(final List<Mutation> mutations) {
        return toIntExact(mutations.stream().filter(Mutation::isErroneous).count());
    }

    /**
     * Counts mutations which are successful.
     *
     * @param mutations all mutations
     * @return number of mutations with errors
     */
    protected int countSuccessful(final List<Mutation> mutations) {
        return toIntExact(mutations.stream().filter(Mutation::isValid).count());
    }
}
