package de.kosit.xmlmutate.mutator;


import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;

import de.kosit.xmlmutate.runner.MutationException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;

import static de.kosit.xmlmutate.mutator.WhitespaceMutator.INTERNAL_PROP_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Testet die Werte-Liste-Variante des {@link GeneriCodeMutationGenerator}s.
 *
 * @author Andreas Penski
 */
public class WhitespaceMutationGeneratorTest {

    private static final String TESTCONTENT = "someText";

    private static final String SPACE = " ";
    private static final String TAB = "\t";
    private static final String NEWLINE = "\n";

    private final WhitespaceMutationGenerator generator = new WhitespaceMutationGenerator();

    @Test
    @DisplayName("Test with more than 1 length parameter declared")
    public void testLengthParameterTooMany() {
        final MutationConfig config = createConfig().add("length", "2").add("length", "4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT))),
                "Structural mismatch: Only 1 length parameter declaration allowed"
        );
    }

    @Test
    @DisplayName("Test with more than 1 length parameter value")
    public void testLengthParameterTooManyValues() {
        final MutationConfig config = createConfig().add("length", "2,4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT))),
                "Structural mismatch: Only 1 length parameter value allowed"
        );
    }

    @Test
    @DisplayName("Test with a length parameter that is not an integer")
    public void testLengthParameterNotInteger() {
        final MutationConfig config = createConfig().add("length", "2.4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT))),
                "Structural mismatch: Length parameter value is not an integer"
        );
    }

    @Test
    @DisplayName("Test for the default whitespace mutator if no parameters are provided")
    public void testDefaultWhitespaceMutator() {
        final MutationConfig config = createConfig();
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT)));
        assertThat(mutations).hasSize(3);
        assertThat(mutations.stream().filter(Objects::isNull).count()).isZero();
        assertThat(mutations.stream().filter(m -> m.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE) != null).count()).isNotZero();
        assertThat(mutationsContainExactly(mutations, WhitespaceMutator.Position.MIX)).isTrue();
    }

    @Test
    @DisplayName("Test for the whitespace mutator with 2 positions provided")
    public void testWhitespaceTwoPositions() {
        final MutationConfig config = createConfig().add("position", "prefix, suffix");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT)));
        assertThat(mutations).hasSize(2);
        assertThat(mutations.stream().filter(Objects::isNull).count()).isZero();
        assertThat(mutations.stream().filter(m -> m.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE) != null).count()).isNotZero();
        assertThat(mutationsContainExactly(mutations, WhitespaceMutator.Position.PREFIX, WhitespaceMutator.Position.SUFFIX)).isTrue();
    }

    @Test
    @DisplayName("Test for the whitespace mutator with repeated positions provided")
    public void testWhitespaceRepeatedPositionValues() {
        final MutationConfig config = createConfig().add("position", "prefix, suffix, prefix");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT)));
        assertThat(mutations).hasSize(2);
        assertThat(mutations.stream().filter(Objects::isNull).count()).isZero();
        assertThat(mutations.stream().filter(m -> m.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE) != null).count()).isNotZero();
        assertThat(mutationsContainExactly(mutations, WhitespaceMutator.Position.PREFIX, WhitespaceMutator.Position.SUFFIX)).isTrue();
    }

    @Test
    @DisplayName("Test for the whitespace mutator with repeated positions provided")
    public void testWhitespaceRepeatedPositionDeclaration() {
        final MutationConfig config = createConfig().add("position", "prefix").add("position", "suffix");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT)));
        assertThat(mutations).hasSize(2);
        assertThat(mutations.stream().filter(Objects::isNull).count()).isZero();
        assertThat(mutations.stream().filter(m -> m.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE) != null).count()).isNotZero();
        assertThat(mutationsContainExactly(mutations, WhitespaceMutator.Position.PREFIX, WhitespaceMutator.Position.SUFFIX)).isTrue();
    }

    @Test
    @DisplayName("Test for the whitespace mutator with list parameter provided")
    public void testWhitespaceListDefined() {
        final MutationConfig config = createConfig().add("list", "space, tab");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT)));
        assertThat(mutations).hasSize(3);
        assertThat(mutations.stream().filter(Objects::isNull).count()).isZero();
        assertThat(mutations.stream().filter(m -> m.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE) != null).count()).isNotZero();
        assertThat(mutationsContainExactly(mutations, WhitespaceMutator.Position.MIX)).isTrue();
        assertThat(mutations.stream().filter(m -> mutatedContentContainsOnly(m.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE).toString(), SPACE, TAB)).count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Test for the whitespace mutator with list parameter provided")
    public void testWhitespaceRepeatedListDeclaration() {
        final MutationConfig config = createConfig().add("list", "newline").add("list", "tab");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext(target -> target.setTextContent(TESTCONTENT)));
        assertThat(mutations).hasSize(3);
        assertThat(mutations.stream().filter(Objects::isNull).count()).isZero();
        assertThat(mutations.stream().filter(m -> m.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE) != null).count()).isNotZero();
        assertThat(mutationsContainExactly(mutations, WhitespaceMutator.Position.MIX)).isTrue();
        assertThat(mutations.stream().filter(m -> mutatedContentContainsOnly(m.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE).toString(), NEWLINE, TAB)).count()).isEqualTo(3);
    }

    private boolean mutatedContentContainsOnly(final String textContent, final String ... characters) {
        final String addedWhitespaces = textContent.replace(TESTCONTENT, "");
        final List<String> onlyCharacters = Arrays.asList(characters);
        final List<String> allCharacters = Arrays.asList(SPACE, TAB, NEWLINE);
        final List<String> forbiddenCharacters = new ArrayList<>(allCharacters);
        forbiddenCharacters.removeAll(onlyCharacters);
        return forbiddenCharacters.stream().noneMatch(addedWhitespaces::contains);
    }

    private boolean mutationsContainExactly(final List<Mutation> mutations, final WhitespaceMutator.Position... positions) {
        final List<Mutation> clone = new ArrayList<>(mutations);
        List<WhitespaceMutator.Position> positionArrayList = Lists.newArrayList(positions);
        if (positions.length == 1 && positions[0] == WhitespaceMutator.Position.MIX) {
             positionArrayList = WhitespaceMutator.Position.getAllPositionsButMix();
        }
        for (final WhitespaceMutator.Position position : positionArrayList) {
            final List<Mutation> result = mutations.stream().filter(m -> m.getIdentifier().contains(position.name())).collect(Collectors.toList());
            if (result.size() > 1) {
                return false;
            } else {
                if(isPositionRight(result.get(0), position)) {
                    clone.remove(result.get(0));
                } else {
                    return false;
                }

            }
        }
        return clone.isEmpty();
    }

    private boolean isPositionRight(final Mutation mutation, final WhitespaceMutator.Position position) {
        boolean isRight = true;

        final String mutatedTextContent = mutation.getConfiguration().getProperties().get(INTERNAL_PROP_VALUE).toString();
        switch(position) {
            case PREFIX:
                if(mutatedTextContent.indexOf(TESTCONTENT) == 0) {
                    isRight = false;
                }
                break;
            case SUFFIX:
                if(mutatedTextContent.indexOf(TESTCONTENT) != 0) {
                    isRight = false;
                }
                break;
            case REPLACE:
                if(mutatedTextContent.contains(TESTCONTENT)) {
                    isRight = false;
                }
                break;
        }
        return isRight;
    }


}
