package de.kosit.xmlmutate.mutator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.runner.Services;

/**
 * Testet die Registry.
 * 
 * @author Andreas Penski
 */
public class MutatorRegistryTest {

    private final MutatorRegistry registry = Services.getRegistry();

    @Test
    @DisplayName("Simple Test Mutator")
    public void testSimpleMutator() {
        assertThat(this.registry.getMutator("remove")).isNotNull();
        assertThat(this.registry.getMutator("remove").getName()).isEqualTo("remove");
        assertThat(this.registry.getMutator("unknown")).isNull();
    }

    @Test
    @DisplayName("Simple Test Generator")
    public void testSimpleGenerator() {
        assertThat(this.registry.getGenerator("text")).isNotNull();
        assertThat(this.registry.getGenerator("text").getName()).isEqualTo("text");
        assertThat(this.registry.getGenerator("unknown")).isNotNull();
        assertThat(this.registry.getGenerator("unknown").getName()).isEqualTo(DefaultMutationGenerator.NAME);
        assertThat(this.registry.getGenerator("remove")).isNotNull();
        assertThat(this.registry.getGenerator("remove").getName()).isEqualTo(DefaultMutationGenerator.NAME);
    }
}
