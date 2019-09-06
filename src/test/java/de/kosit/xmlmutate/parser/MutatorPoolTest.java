package de.kosit.xmlmutate.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * MutatorPoolTest
 */
public class MutatorPoolTest {

    @Test
    @DisplayName("has Remove Muator")
    public void notEmptyPool() {
        assertNotNull(MutatorPool.getMutator("remove"));
        assertEquals(MutatorPool.getMutator("remove").getName(), "remove");
    }
}
