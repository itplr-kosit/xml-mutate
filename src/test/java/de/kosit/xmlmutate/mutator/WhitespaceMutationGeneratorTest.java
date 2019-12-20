package de.kosit.xmlmutate.mutator;


import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;

import de.kosit.xmlmutate.runner.MutationException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Testet die Werte-Liste-Variante des {@link CodeMutationGenerator}s.
 *
 * @author Andreas Penski
 */
public class WhitespaceMutationGeneratorTest {

    private final WhitespaceMutationGenerator generator = new WhitespaceMutationGenerator();

    @Test
    @DisplayName("Test with more than 1 length parameter declared")
    public void testLengthParameterTooMany() {
        final MutationConfig config = createConfig().add("length", "2").add("length", "4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext(target -> target.setTextContent("someText"))),
                "Structural mismatch: Only 1 length parameter declaration allowed"
        );
    }

    @Test
    @DisplayName("Test with more than 1 length parameter value")
    public void testLengthParameterTooManyValues() {
        final MutationConfig config = createConfig().add("length", "2,4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext(target -> target.setTextContent("someText"))),
                "Structural mismatch: Only 1 length parameter value allowed"
        );
    }

    @Test
    @DisplayName("Test with a length parameter that is not an integer")
    public void testLengthParameterNotInteger() {
        final MutationConfig config = createConfig().add("length", "2.4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext(target -> target.setTextContent("someText"))),
                "Structural mismatch: Length parameter value is not an integer"
        );
    }

    @Test
    @DisplayName("Test for the default whitespace mutator if no parameters are provided")
    public void testDefaultWhitespaceMutator() {
        final MutationConfig config = createConfig();
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext(target -> target.setTextContent("someText")));
        assertThat(mutations).hasSize(3);
        assertThat(mutations.stream().filter(Objects::isNull).count()).isZero();
        assertThat(mutations.stream().filter(m -> m.getConfiguration().getProperties().get(WhitespaceMutator.INTERNAL_PROP_VALUE) != null).count()).isNotZero();
        assertThat(mutationsContainExactly(mutations, WhitespaceMutator.Position.MIX)).isTrue();
    }

    @Test
    @DisplayName("Test for the whitespace mutator with 2 positions provided")
    public void testWhitespaceTwoPositions() {
        final MutationConfig config = createConfig().add("position", "prefix, suffix");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext(target -> target.setTextContent("someText")));
        assertThat(mutations).hasSize(2);
        assertThat(mutations.stream().filter(Objects::isNull).count()).isZero();
        assertThat(mutations.stream().filter(m -> m.getConfiguration().getProperties().get(WhitespaceMutator.INTERNAL_PROP_VALUE) != null).count()).isNotZero();
        assertThat(mutationsContainExactly(mutations, WhitespaceMutator.Position.PREFIX, WhitespaceMutator.Position.SUFFIX)).isTrue();
    }

    private boolean mutationsContainExactly(final List<Mutation> mutations, final WhitespaceMutator.Position... positions) {
        final List<Mutation> clone = new ArrayList<>(mutations);
        List<WhitespaceMutator.Position> positionsArray = Lists.newArrayList(positions);
        if (positions.length == 1 && positions[0] == WhitespaceMutator.Position.MIX) {
            positionsArray = WhitespaceMutator.Position.getAllPositionsButMix();
        }
        for (final WhitespaceMutator.Position position : positionsArray) {
            final List<Mutation> result = mutations.stream().filter(m -> m.getIdentifier().contains(position.name())).collect(Collectors.toList());
            if (result.size() > 1) {
                return false;
            } else {
                clone.remove(result.get(0));
            }
        }
        return clone.isEmpty();
    }


}
