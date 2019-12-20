package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.TestResource.asPath;
import static de.kosit.xmlmutate.assertions.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.TestResource.TransformResource;

/**
 * Tests various inputs for {@link de.kosit.xmlmutate.mutator.TransformationMutator}.
 * 
 * @author Andreas Penski
 */
public class TransformationMutatorCompleteTest extends CompleteRun {

    @Test
    public void testSimple() {
        final RunnerConfig config = createConfig(TransformResource.BOOK_XML);
        config.addTemplate("simple", asPath(TransformResource.SIMPLE_TRANSFORM));
        final RunnerResult result = run(config);
        assertThat(result).isSucessful();
        assertThat(result).hasMutationCount(1);
        // TODO read result Document and assert nodes
    }

    @Test
    public void testMissingTemplate() {
        final RunnerConfig config = createConfig(TransformResource.BOOK_XML);
        final RunnerResult result = run(config);
        assertThat(result).isErroneous();
        assertThat(result).hasMutationCount(1);
        assertThat(result.getMutation(0)).containsError("Template \"simple\" not found");
    }

    @Test
    public void testInvlidTemplate() {
        final RunnerConfig config = createConfig(TransformResource.BOOK_XML);
        config.addTemplate("simple", asPath(TransformResource.INVALD_TRANSFORM));
        assertThrows(IllegalArgumentException.class, () -> run(config));
    }


}
