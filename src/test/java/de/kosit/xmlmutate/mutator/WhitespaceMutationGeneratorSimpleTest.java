package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;

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
        final MutationConfig config = createConfig().add("options", "2,4,6");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(3);
    }

//    @Test
//    public void testEmptyConfig() {
//        final MutationConfig config = createConfig().add("options", "");
//        assertThrows(MutationException.class, () -> {
//            this.generator.generateMutations(config, createContext());
//        });
//    }
//
//    @Test
//    public void testNoConfig() {
//        assertThrows(MutationException.class, () -> {
//            this.generator.generateMutations(createConfig(), createContext());
//        });
//    }

}
