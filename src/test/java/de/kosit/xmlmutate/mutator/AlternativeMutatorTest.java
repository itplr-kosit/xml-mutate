package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Comment;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;

/**
 * @author Andreas Penski
 */
public class AlternativeMutatorTest {

    @Test
    public void testSimple() {
        final MutationContext context = createContext(target -> {
            final Comment sub = target.getOwnerDocument().createComment("<some>xml</some><with>2nodes</with>");
            target.appendChild(sub);
        });
        final MutationConfig config = createConfig().add(AlternativeMutator.ALT_KEY, "0");
        new AlternativeMutator().mutate(context, config);
        System.out.println(TestHelper.serialize(context.getDocument()));

    }
}
