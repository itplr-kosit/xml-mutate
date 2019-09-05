package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * Simple mutator which does not make any changes to the documents.
 * Purpose is to check schematron assertions of the given document.
 */
@Slf4j
public class IdentityMutator implements Mutator {
    @Override
    public List<String> getNames() {
        return Arrays.asList("identity", "noop");
    }

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().get();
    }

    @Override
    public void mutate(MutationContext context, MutationConfig config) {
        log.debug("Mutating e.g. do 'nothing'");
    }
}
