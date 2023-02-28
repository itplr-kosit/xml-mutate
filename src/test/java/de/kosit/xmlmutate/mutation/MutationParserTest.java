package de.kosit.xmlmutate.mutation;

import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronEnterity;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * It tests the basic parser functions
 *
 * @author Andreas Penski
 */
public class MutationParserTest {

    private final MutationParser parser = new MutationParser();

    @Test
    @DisplayName("Alias test")
    public void alias() {
        final MutationDocumentContext context = createContext("mutator=noop");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations.get(0).getMutator().getClass().getSimpleName()).isEqualToIgnoringCase("IdentityMutator");
    }

    @Test
    @DisplayName("Simple Test")
    public void simple() {
        final MutationDocumentContext context = createContext("mutator=remove");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
    }

    @Test
    @DisplayName("Test quoted parameter")
    public void quotedParameter() {
        final MutationDocumentContext context = createContext(
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
        final MutationDocumentContext context = createContext(
                "mutator=remove schematron-valid=\"ubl:BR-52 ubl:BR-CO-25 ubl:BR-63 ubl:BR-11 ubl:BR-51 ubl:BR-57 ubl:BR-31 ubl:BR-32 ubl:BR-33 ubl:BR-CO-05 ubl:BR-CO-21 ubl:BR-DEC-01 ubl:BR-DEC-02 ubl:BR-36 ubl:BR-37 ubl:BR-38\"");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        final List<SchematronRuleExpectation> e = mutations.get(0).getConfiguration().getSchematronExpectations();
        assertEquals(16, e.size());

        assertEquals("BR-52", e.get(0).getRuleName());
        assertEquals("ubl", e.get(0).getSource());

        // assertThat(m.getConfiguration().getProperties().get("test2")).isEqualTo("value
        // value");
    }

    @Test
    @DisplayName("Test comma separated parameter")
    public void commaSeparated() {
        final MutationDocumentContext context = createContext("mutator=remove key=\"val1,val2\"");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties()).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties().get("key")).isEqualTo("val1,val2");
    }

    @Test
    @DisplayName("Test Multiple  parameter declaration")
    public void testMultipleParameter() {
        final MutationDocumentContext context = createContext("mutator=remove key=val key=val2");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties()).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties().get("key")).isInstanceOf(Collection.class);
    }

    @Test
    @DisplayName("Test unknown mutator")
    public void testUnknowMutator() {
        final MutationDocumentContext context = createContext("mutator=unknown");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(e -> StringUtils.containsIgnoreCase(e.getMessage(), "No valid mutator found for unknown"))).isTrue();
    }

    @Test
    @DisplayName("Test no mutator configuration")
    public void testNoMutator() {
        final MutationDocumentContext context = createContext("schema-valid");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(e -> StringUtils.containsIgnoreCase(e.getMessage(), "mismatched input 'schema-' expecting 'mutator'"))).isTrue();
    }

    @Test
    @DisplayName("Test parsing error")
    public void testParsingError() {
        final MutationDocumentContext context = createContext("mutator=remove schema-val");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(e -> StringUtils.containsIgnoreCase(e.getMessage(), "missing {'valid', 'invalid'} at 'val'"))).isTrue();
    }

    @Test
    @DisplayName("Test no target identification possible")
    public void testNoTarget() {
        final MutationDocumentContext context = createContext("mutator=remove");
        // remove target
        context.getParentElement().removeChild(context.getTarget());

        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(
                e -> StringUtils.containsIgnoreCase(e.getMessage(), "No mutation can be found for dummy.xml. Is PI last element?")))
                .isTrue();
    }

    @Test
    @DisplayName("Test with a PI with correct id and tag names")
    public void testCorrectIdAndTags() {
        final MutationDocumentContext context = createContext("mutator=remove id=\"id1\" tag=\"tag1, tag2\"");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.CREATED);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isEqualTo(0);
        assertThat(Collections.disjoint(mutations.get(0).getConfiguration().getTagNames(), Arrays.asList("tag1", "tag2"))).isFalse();
    }

    @Test
    @DisplayName("Test with a PI with an empty id")
    public void testEmptyId() {
        final MutationDocumentContext context = createContext("mutator=remove id=\"\"");
        // remove target
        context.getParentElement().removeChild(context.getTarget());

        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(e -> StringUtils.containsIgnoreCase(e.getMessage(), "Mutation instruction id can not be empty"))).isTrue();
    }

    @Test
    @DisplayName("Test with a PI with an empty tag")
    public void testEmptyTag() {
        final MutationDocumentContext context = createContext("mutator=remove tag=\"\"");
        // remove target
        context.getParentElement().removeChild(context.getTarget());

        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(e -> StringUtils.containsIgnoreCase(e.getMessage(), "Mutation instruction tag can not be empty"))).isTrue();
    }

    @Test
    @DisplayName("Test with a PI with several ids")
    public void testSeveralIds() {
        final MutationDocumentContext context = createContext("mutator=remove id=\"id1, id2\"");
        // remove target
        context.getParentElement().removeChild(context.getTarget());

        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(e -> StringUtils.containsIgnoreCase(e.getMessage(), "Mutation instruction can only have 1 id"))).isTrue();
    }

    @Test
    @DisplayName("schematronKeyword with all valid")
    public void schematronAllValid() {
        final MutationDocumentContext context = createContext(
                "mutator=remove schematron-valid-all");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getSchematronExpectations()).isEmpty();
        assertThat(mutations.get(0).getConfiguration().getSchematronEnterityExpectation().getKey()).isEqualTo(SchematronEnterity.ALL);
        assertThat(mutations.get(0).getConfiguration().getSchematronEnterityExpectation().getValue()).isEqualTo(ExpectedResult.PASS);
    }

    @Test
    @DisplayName("schematronKeyword with none invalid")
    public void schematronNoneInvalid() {
        final MutationDocumentContext context = createContext(
                "mutator=remove schematron-invalid-none");
        final List<Mutation> mutations = this.parser.parse(context);
        assertValid(mutations);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getSchematronExpectations()).isEmpty();
        assertThat(mutations.get(0).getConfiguration().getSchematronEnterityExpectation().getKey()).isEqualTo(SchematronEnterity.NONE);
        assertThat(mutations.get(0).getConfiguration().getSchematronEnterityExpectation().getValue()).isEqualTo(ExpectedResult.FAIL);
    }

    @Test
    @DisplayName("schematronKeyword with all valid and schematron rules")
    public void schematronEntertyMixedWithRules() {
        final MutationDocumentContext context = createContext(
                "mutator=remove schematron-valid-all=\"BR-DE-1\"");
        final List<Mutation> mutations = this.parser.parse(context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutations.get(0).getConfiguration().getSchematronExpectations()).isEmpty();
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages()).isNotEmpty();
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
