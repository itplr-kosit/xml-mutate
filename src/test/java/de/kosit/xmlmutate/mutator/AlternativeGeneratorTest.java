package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationContext;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;

import java.util.List;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.mutator.AlternativeMutator.ALT_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Tests the generation of mutations for the {@link AlternativeMutator}.
 */
public class AlternativeGeneratorTest {

    private AlternativeMutator generator = new AlternativeMutator();

    @Test
    public void simpleTest() {
        MutationContext context = createContext(node -> {
            Comment comment = node.getOwnerDocument().createComment("some text");
            node.appendChild(comment);
        });
        List<Mutation> mutations = generator.generateMutations(createConfig(), context);
        assertThat(mutations).hasSize(1);
        assertThat(mutations.get(0).getConfiguration().getProperties()).contains(entry(ALT_KEY, 0));
    }

    @Test
    public void noMutationTest() {
        MutationContext context = createContext();
        List<Mutation> mutations = generator.generateMutations(createConfig(), context);
        assertThat(mutations).hasSize(0);
    }

    @Test
    public void multipleMutationsTests() {
        MutationContext context = createContext(node -> {
            Document document = node.getOwnerDocument();
            node.appendChild(document.createComment("some text"));
            node.appendChild(document.createComment("some more text"));
        });
        List<Mutation> mutations = generator.generateMutations(createConfig(), context);
        assertThat(mutations).hasSize(2);
    }

}