package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.runner.Services;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * It tests the registry
 * 
 * @author Andreas Penski
 */
public class MutatorRegistryTest {

    private final MutatorRegistry registry = Services.getRegistry();

    @Test
    @DisplayName("Simple Test Mutator")
    public void testSimpleMutator() {
        assertThat(this.registry.getMutator("remove")).isNotNull();
        assertThat(this.registry.getMutator("remove").getPreferredName()).isEqualTo("remove");
        assertThat(this.registry.getMutator("unknown")).isNull();
    }

    @Test
    @DisplayName("Simple Test Generator")
    public void testSimpleGenerator() {
        assertThat(this.registry.getGenerator("text")).isNotNull();
        assertThat(this.registry.getGenerator("text").getPreferredName()).isEqualTo("text");
        assertThat(this.registry.getGenerator("unknown")).isNotNull();
        assertThat(this.registry.getGenerator("unknown").getPreferredName()).isEqualTo(DefaultMutationGenerator.NAME);
        assertThat(this.registry.getGenerator("remove")).isNotNull();
        assertThat(this.registry.getGenerator("remove").getPreferredName()).isEqualTo(DefaultMutationGenerator.NAME);
    }
}
