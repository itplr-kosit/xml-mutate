package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testet den Whitespace Mutator
 *
 * @author Victor del Campo
 */
public class WhitespaceMutatorTest {

    private final WhitespaceMutator mutator = new WhitespaceMutator();

    @Test
    @DisplayName("Test for the default whitespace mutator")
    void testSimpleNoParameters() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        this.mutator.mutate(context, createConfig());
        // Default whitespace mutator is position replace and length 5, and >= since CRLF will be 2 characters
        assertThat(context.getTarget().getTextContent().length()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Test with all possible whitespace mutator parameters and prefix position")
    void testAllParametersPrefix() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        this.mutator.mutate(context,
                createConfig().add("position", "prefix")
                        .add("length", "7")
                        .add("list", "cr,lf,tab"));
        assertThat(whitespacesArePrefix(context.getTarget().getTextContent())).isTrue();
        assertThat(context.getTarget().getTextContent()).hasSize("someText".length() + 7);
        assertThat(containsNone(context.getTarget().getTextContent(), Arrays.asList("space"))).isTrue();
    }

    @Test
    @DisplayName("Test with all possible whitespace mutator parameters and suffix position")
    void testAllParametersSuffix() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        this.mutator.mutate(context,
                createConfig().add("position", "suffix")
                        .add("length", "12")
                        .add("list", "lf,tab"));
        assertThat(whitespacesAreSuffix(context.getTarget().getTextContent())).isTrue();
        assertThat(context.getTarget().getTextContent()).hasSize("someText".length() + 12);
        assertThat(containsNone(context.getTarget().getTextContent(), Arrays.asList("space"))).isTrue();
    }

    @Test
    @DisplayName("Test with too many position parameter declaration")
    void testTooManyPositionDeclaration() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig().add("position", "prefix").add("position", "suffix"));
        });
    }

    @Test
    @DisplayName("Test with too many length parameter declaration")
    void testTooManyLengthDeclaration() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig().add("length", "5").add("length", "6"));
        });
    }

    @Test
    @DisplayName("Test with too many position parameter value declaration")
    void testTooManyPositionValues() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig().add("position", "prefix, suffix"));
        });
    }

    @Test
    @DisplayName("Test with too many length parameter value declaration")
    void testTooManylengthValues() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig().add("length", "5, 6"));
        });
    }

    @Test
    @DisplayName("Test with a length value but not integer")
    void testNotIntLengthValue() {
        final MutationContext context = createContext(target -> {
            target.setTextContent("someText");
        });
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig().add("length", "5.6"));
        });
    }

    @Test
    void testEmtpyTarget() {
        final MutationContext context = createContext(target -> {
        });
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, createConfig());
        });
    }

    @Test
    void testComplexStructure() {
        final MutationContext context = createContext(target -> {
            final Document doc = target.getOwnerDocument();
            final Element sub = doc.createElement("sub");
            final Element subsub = doc.createElement("subsub");
            sub.appendChild(subsub);
            target.appendChild(sub);
        });
        final MutationConfig config = createConfig();
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, config);
        });
    }

    private boolean containsNone(final String textContent, final List<String> forbidden) {
        for (final String entry : forbidden) {
            if (textContent.contains(WhitespaceMutator.XmlWhitespaceCharacter.fromString(entry.toUpperCase()).getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean whitespacesArePrefix(final String textContent) {
        return textContent.indexOf("someText") != 0;
    }

    private boolean whitespacesAreSuffix(final String textContent) {
        return textContent.indexOf("someText") == 0;
    }
}
