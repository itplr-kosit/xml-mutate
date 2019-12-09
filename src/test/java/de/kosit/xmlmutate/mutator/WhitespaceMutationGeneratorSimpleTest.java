package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.runner.MutationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testet  {@link WhitespaceMutationGenerator}
 * 
 * @author Victor del Campo
 */
public class WhitespaceMutationGeneratorSimpleTest {

    private final WhitespaceMutationGenerator generator = new WhitespaceMutationGenerator();

    @Test
    @DisplayName("Test for a default whitespace mutator, where all variations (6) are performed")
    public void testSimpleDefaultOptions() {
        final MutationConfig config = createConfig();
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(6);
    }

    @Test
    @DisplayName("Test for a whitespace mutator with a selection of 3 variations")
    public void testNoDefaultOnly3Options() {
        final MutationConfig config = createConfig().add("variations", "2,4,6");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(3);
    }

    @Test
    @DisplayName("Test for a whitespace mutator with a PI with the keyword variations twice")
    public void testNoDefaultSeveralKeywords() {
        final MutationConfig config = createConfig().add("variations", "2").add("variations", "4,6");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(3);
    }

    @Test
    public void testEmptyConfig() {
        final MutationConfig config = createConfig().add("variations", "");
        assertThrows(MutationException.class, () -> {
            this.generator.generateMutations(config, createContext());
        });
    }

}
