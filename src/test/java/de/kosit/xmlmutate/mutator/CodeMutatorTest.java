package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.assertions.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.runner.MutationException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * It tests the code mutator with values.
 *
 * @author Andreas Penski
 */
public class CodeMutatorTest {

    private final CodeMutator mutator = new CodeMutator();

    @Test
    public void testContent() {
        final MutationDocumentContext context = createContext();
        final MutationConfig config = createConfig().add(CodeMutator.INTERNAL_PROP_VALUE, "T1");
        this.mutator.mutate(context, config);
        assertThat(context.getTarget()).isNotNull();
        assertThat(context.getTarget().getTextContent()).isEqualTo("T1");
    }

    @Test
    public void testAttribute() {
        final String attrName = "attr";
        final String value = "T1";
        final MutationDocumentContext context = createContext(target ->
            target.setAttribute(attrName, "value"));
        final MutationConfig config = createConfig().add(
            CodeMutator.INTERNAL_PROP_VALUE, value).add("attribute", attrName);
        this.mutator.mutate(context, config);
        assertThat(context.getTarget()).hasAttribute(attrName);
        assertThat(context.getTarget().getAttributes().getNamedItem(attrName)).hasTextContent(value);
    }

    @Test
    public void testUnexistingAttribute() {
        final String attrName = "attr";
        final MutationDocumentContext context = createContext();
        final MutationConfig config = createConfig().add(
            CodeMutator.INTERNAL_PROP_VALUE, "T1").add("attribute", attrName);
        assertThrows(MutationException.class, () -> this.mutator.mutate(context, config));
    }

    @Test
    public void testComplexStructure() {
        final MutationDocumentContext context = createContext(target -> {
            final Document doc = target.getOwnerDocument();
            final Element sub = doc.createElement("sub");
            final Element subsub = doc.createElement("subsub");
            sub.appendChild(subsub);
            target.appendChild(sub);
        });
        final MutationConfig config = createConfig().add(CodeMutator.INTERNAL_PROP_VALUE, "T1");
        assertThrows(MutationException.class, () -> this.mutator.mutate(context, config));
    }

}
