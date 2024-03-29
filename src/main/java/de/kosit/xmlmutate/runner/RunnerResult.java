// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
package de.kosit.xmlmutate.runner;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * The Result of the xml mutate run.
 *
 * @author Andreas Penski
 */
public class RunnerResult {
    private final List<Pair<Path, List<Mutation>>> result;

    /**
     * Checks whether there is a Mutation which was not successfuil.
     *
     * @return true when not sucessful
     */
    public boolean isErrorPresent() {
        return CollectionUtils.isNotEmpty(this.result) && this.result.stream().flatMap(e -> e.getValue().stream()).anyMatch(Mutation::isErroneousOrContainsErrorMessages);
    }

    public boolean isSuccessful() {
        return !isErrorPresent();
    }

    Mutation getMutation(final int index) {
        return getMutation(0, index);
    }

    public Mutation getMutation(final int inputIndex, final int mutationIndex) {
        if (this.result.size() > inputIndex && this.result.get(inputIndex).getRight().size() > mutationIndex) {
            return this.result.get(inputIndex).getValue().get(mutationIndex);
        }
        throw new IllegalArgumentException(String.format("No such mutation for coordinates %s,%s", inputIndex, mutationIndex));
    }

    public RunnerResult(final List<Pair<Path, List<Mutation>>> result) {
        this.result = result;
    }

    public List<Pair<Path, List<Mutation>>> getResult() {
        return this.result;
    }
}
