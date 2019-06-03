package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Testet die Werte-Liste-Variante des {@link CodeMutationGenerator}s.
 * 
 * @author Andreas Penski
 */
public class CodeMutationGeneratorSimpleTest {

    private final CodeMutationGenerator generator = new CodeMutationGenerator();

    @Test
    public void testSimpleValue() {
        final MutationConfig config = createConfig().add("values", "test");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getMutator()).isNotNull();
        assertThat(mutations.get(0).getConfiguration().getProperties()).containsEntry(CodeMutator.INTERNAL_PROP_VALUE, "test");
    }

    @Test
    public void testMultipleValue() {
        final MutationConfig config = createConfig().add("values", "test,test2");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(2);
        assertThat(mutations.get(0).getConfiguration().getProperties()).containsEntry(CodeMutator.INTERNAL_PROP_VALUE, "test");
        assertThat(mutations.get(1).getConfiguration().getProperties()).containsEntry(CodeMutator.INTERNAL_PROP_VALUE, "test2");
    }

    @Test
    public void testEmptyConfig() {
        final MutationConfig config = createConfig().add("values", "");
        assertThrows(MutationException.class, () -> {
            this.generator.generateMutations(config, createContext());

        });
    }

    @Test
    public void testNoConfig() {
        assertThrows(MutationException.class, () -> {
            this.generator.generateMutations(createConfig(), createContext());
        });
    }

}
