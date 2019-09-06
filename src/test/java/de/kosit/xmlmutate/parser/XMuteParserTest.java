package de.kosit.xmlmutate.parser;

import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.parser.MutatorInstruction;
import de.kosit.xmlmutate.parser.XMuteParser;
import lombok.extern.slf4j.Slf4j;

/**
 * Testet die grundsätzlichen Parser-Funktionen.
 *
 * @author Andreas Penski
 */
@Slf4j
public class XMuteParserTest {

    private final XMuteParser parser = new XMuteParser();
    final String REMOVE_MUTATOR_NAME = "remove";
    final String remove_data = "mutator=\"" + REMOVE_MUTATOR_NAME + "\"";

    private void assertValidInstruction(MutatorInstruction instruction, String mutatorName) {
        assertNotNull(instruction.getMutatorName());
        assertEquals(mutatorName, instruction.getMutatorName());
        // assertTrue(false);
    }

    @Test
    @DisplayName("Mutator only test")
    public void simple() {

        log.trace("testing={}", remove_data);
        final MutatorInstruction instruction = this.parser.parse(remove_data);
        assertNotNull(instruction);
        this.assertValidInstruction(instruction, REMOVE_MUTATOR_NAME);
        // assertThat(instruction).hasSize(1);
    }

    @Test
    @DisplayName("Test schematron-valid=\"RP-1\"")
    public void schematronValidOneRule() {
        final String SCH_RULE = remove_data + " schematron-valid=\"RP-1\"";
        log.trace("testing={}", SCH_RULE);
        final MutatorInstruction instruction = this.parser.parse(SCH_RULE);
        assertNotNull(instruction);
        this.assertValidInstruction(instruction, REMOVE_MUTATOR_NAME);

    }

    @Test
    @DisplayName("Mutator name without quotes")
    @Disabled
    public void mutatorNoQuotes() {
        final String MUTATOR_NAME = "remove";
        final String data = "mutator=" + MUTATOR_NAME;
        log.trace("testing={}", data);
        final MutatorInstruction instruction = this.parser.parse(data);
        assertNull(instruction);
        this.assertValidInstruction(instruction, MUTATOR_NAME);
        // assertThat(instruction).hasSize(1);
    }

    @Test
    @DisplayName("Test quoted parameter")
    @Disabled
    public void quotedParameter() {
        final MutationContext context = createContext(
                "mutator=remove \"test\"=\"value value\" test2=\"value value\" \"test3\"=value");
        // final List<Mutation> mutations = this.parser.parse(context);
        // // assertValid(mutations);
        // assertThat(mutations).hasSize(1);
        // assertThat(mutations.get(0).getConfiguration().getProperties()).hasSize(3);
        // assertThat(mutations.get(0).getConfiguration().getProperties().get("test2")).isEqualTo("value
        // value");
    }

    @Test
    @DisplayName("schematronKeyword with many rules")
    @Disabled
    public void manySchematronRules() {
        final MutationContext context = createContext(
                "mutator=remove schematron-valid=\"ubl:BR-52 ubl:BR-CO-25 ubl:BR-63 ubl:BR-11 ubl:BR-51 ubl:BR-57 ubl:BR-31 ubl:BR-32 ubl:BR-33 ubl:BR-CO-05 ubl:BR-CO-21 ubl:BR-DEC-01 ubl:BR-DEC-02 ubl:BR-36 ubl:BR-37 ubl:BR-38\"");
        // final List<Mutation> mutations = this.parser.parse(context);
        // // assertValid(mutations);
        // assertThat(mutations).hasSize(1);
        // List<SchematronRuleExpectation> e =
        // mutations.get(0).getConfiguration().getSchematronExpectations();
        // assertEquals(16, e.size());

        // assertEquals("BR-52", e.get(0).getRuleName());
        // assertEquals("ubl", e.get(0).getSource());

        // assertThat(m.getConfiguration().getProperties().get("test2")).isEqualTo("value
        // value");
    }

    @Test
    @DisplayName("Test comma separated parameter")
    @Disabled
    public void commaSeparated() {
        final MutationContext context = createContext("mutator=remove key=\"val1,val2\"");
        // final List<Mutation> mutations = this.parser.parse(context);
        // // assertValid(mutations);
        // assertThat(mutations).hasSize(1);
        // assertThat(mutations.get(0).getConfiguration().getProperties()).hasSize(1);
        // assertThat(mutations.get(0).getConfiguration().getProperties().get("key")).isEqualTo("val1,val2");
    }

    @Test
    @DisplayName("Test Multiple  parameter declaration")
    @Disabled
    public void testMultipleParameter() {
        final MutationContext context = createContext("mutator=remove key=val key=val2");
        // final List<Mutation> mutations = this.parser.parse(context);
        // assertValid(mutations);
        // assertThat(mutations).hasSize(1);
        // assertThat(mutations.get(0).getConfiguration().getProperties()).hasSize(1);
        // assertThat(mutations.get(0).getConfiguration().getProperties().get("key")).isInstanceOf(Collection.class);
    }

    @Test
    @DisplayName("Test unknown mutator")
    @Disabled
    public void testUnknowMutator() {
        // final MutationContext context = createContext("mutator=unknown");
        // final List<Mutation> mutations = this.parser.parse(context);
        // assertThat(mutations).hasSize(1);
        // assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        // assertThat(mutations.get(0).getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Test no mutator configuration")
    @Disabled
    public void testNoMutator() {
        // final MutationContext context = createContext("schema-valid");
        // final List<Mutation> mutations = this.parser.parse(context);
        // assertThat(mutations).hasSize(1);
        // assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        // assertThat(mutations.get(0).getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Test parsing error")
    @Disabled
    public void testParsingError() {
        // final MutationContext context = createContext("mutator=remove schema-val");
        // final List<Mutation> mutations = this.parser.parse(context);
        // assertThat(mutations).hasSize(1);
        // assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        // assertThat(mutations.get(0).getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Test no target identification possible")
    @Disabled
    public void testNoTarget() {
        final String data = "mutator=remove";
        final MutationContext context = createContext();
        // remove target
        context.getParentElement().removeChild(context.getTarget());

        // final List<Mutation> mutations = this.parser.parse(data);
        // assertThat(mutations).hasSize(1);
        // assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        // assertThat(mutations.get(0).getErrorMessage()).isNotEmpty();
    }

}
