package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.assertions.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.TestResource;
import de.kosit.xmlmutate.TestResource.BookResources;

/**
 * @author Andreas Penski
 */
public class TestLoadDocument extends CompleteRun {

    @Test
    public void testLoadSimple() {
        final RunnerConfig config = createConfig(BookResources.NO_SCHEMATRON);
        final RunnerResult result = run(config);
        assertThat(result).isSucessful();
    }

    @Test
    public void testLoadSOhneNamespace() {
        final RunnerConfig config = createConfig(TestResource.TEST_ROOT.resolve("parser/ohne_namespace.xml"));
        config.setIgnoreSchemaInvalidity(true);
        final RunnerResult result = run(config);
        assertThat(result).isErroneous();
        assertThat(result).hasMutationCount(1);
    }
}
