package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.runner.MutationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Testet den Whitespace Mutator
 *
 * @author Victor del Campo
 */
public class WhitespaceMutatorTest {

    private final WhitespaceMutator mutator = new WhitespaceMutator();

    @Test
    @DisplayName("Test for a simple replacement")
    void testSimpleNoParameters() {
        final MutationDocumentContext context = createContext(target -> target.setTextContent("someText"));
        this.mutator.mutate(context, createConfig().add(WhitespaceMutator.INTERNAL_PROP_VALUE, "    someText"));
        assertThat(context.getTarget().getTextContent()).isEqualTo("    someText");
    }

    @Test
    void testEmtpyTarget() {
        final MutationDocumentContext context = createContext(target -> {
        });
        assertThrows(MutationException.class, () -> this.mutator.mutate(context, createConfig()));
    }

    @Test
    void testComplexStructure() {
        final MutationDocumentContext context = createContext(target -> {
            final Document doc = target.getOwnerDocument();
            final Element sub = doc.createElement("sub");
            final Element subsub = doc.createElement("subsub");
            sub.appendChild(subsub);
            target.appendChild(sub);
        });
        final MutationConfig config = createConfig();
        assertThrows(MutationException.class, () -> this.mutator.mutate(context, config));
    }


}
