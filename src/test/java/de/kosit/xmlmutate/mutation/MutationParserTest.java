package de.kosit.xmlmutate.mutation;

import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutation.Mutation.State;

/**
 * Testet die grundsätzlichen Parser-Funktionen.
 *
 * @author Andreas Penski
 */
public class MutationParserTest {

    private final MutationParser parser = new MutationParser();

    @Test
    @DisplayName("Simple Test")
    public void simple() {
        final MutationContext context = createContext("mutator=remove");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
    }

    @Test
    @DisplayName("Test quoted parameter")
    public void quotedParameter() {
        final MutationContext context = createContext(
                "mutator=remove \"test\"=\"value value\" test2=\"value value\" \"test3\"=value");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties()).hasSize(3);
        assertThat(mutations.get(0).getConfiguration().getProperties().get("test2")).isEqualTo("value value");
    }

    @Test
    @DisplayName("schematronKeyword with many rules")
    public void manySchematronRules() {
        final MutationContext context = createContext(
                "mutator=remove schematron-valid=\"ubl:BR-52 ubl:BR-CO-25 ubl:BR-63 ubl:BR-11 ubl:BR-51 ubl:BR-57 ubl:BR-31 ubl:BR-32 ubl:BR-33 ubl:BR-CO-05 ubl:BR-CO-21 ubl:BR-DEC-01 ubl:BR-DEC-02 ubl:BR-36 ubl:BR-37 ubl:BR-38\"");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        List<SchematronRuleExpectation> e = mutations.get(0).getConfiguration().getSchematronExpectations();
        assertEquals(16, e.size());

        assertEquals("BR-52", e.get(0).getRuleName());
        assertEquals("ubl", e.get(0).getSchematronSource());

        // assertThat(m.getConfiguration().getProperties().get("test2")).isEqualTo("value
        // value");
    }

    @Test
    @DisplayName("Test comma separated parameter")
    public void commaSeparated() {
        final MutationContext context = createContext("mutator=remove key=\"val1,val2\"");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties()).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties().get("key")).isEqualTo("val1,val2");
    }

    @Test
    @DisplayName("Test Multiple  parameter declaration")
    public void testMultipleParameter() {
        final MutationContext context = createContext("mutator=remove key=val key=val2");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties()).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties().get("key")).isInstanceOf(Collection.class);
    }

    @Test
    @DisplayName("Test unknown mutator")
    public void testUnknowMutator() {
        final MutationContext context = createContext("mutator=unknown");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Test no mutator configuration")
    public void testNoMutator() {
        final MutationContext context = createContext("schema-valid");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Test parsing error")
    public void testParsingError() {
        final MutationContext context = createContext("mutator=remove schema-val");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Test no target identification possible")
    public void testNoTarget() {
        final MutationContext context = createContext("mutator=remove");
        // remove target
        context.getParentElement().removeChild(context.getTarget());

        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getErrorMessage()).isNotEmpty();
    }

    private void assertValid(final List<Mutation> mutations) {
        mutations.forEach(e -> {
            assertThat(e.getContext()).isNotNull();
            assertThat(e.getConfiguration()).isNotNull();
            assertThat(e.getMutator()).isNotNull();
            assertThat(e.getState()).isEqualTo(State.CREATED);
            assertThat(e.getIdentifier()).isNotNull();
        });
    }

}
