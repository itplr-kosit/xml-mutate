package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.TEST_ROOT;
import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.TemplateRepository;

/**
 * Tests the Generator for {@link TransformationMutator}.
 * 
 * @author Andreas Penski
 */
public class TransformationMutationGeneratorTest {

    private static final URI SIMPLE_TRANSFORMATION = TEST_ROOT.resolve("transform/simple.xsl");

    private static final String SIMPLE_NAME = "simple";

    private TemplateRepository repository;

    private TransformationMutationGenerator generator;

    @BeforeEach
    public void setup() {
        this.repository = new TemplateRepository();
        this.generator = new TransformationMutationGenerator(this.repository);
    }

    @Test
    @Disabled
    public void testSimpleGenerate() {
        final MutationConfig config = createConfig().add("name", SIMPLE_NAME);
        this.repository.registerTemplate(SIMPLE_NAME, SIMPLE_TRANSFORMATION);
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(1);
        final Mutation actual = mutations.get(0);
        assertThat((Object) actual.getConfiguration().getProperty(TransformationMutator.TEMPLATE_PARAM)).isNotNull();
        final Map<String, String> parameters = actual.getConfiguration().getProperty(TransformationMutator.PARAMETER_PARAM);
        assertThat(parameters).isNotNull();
        assertThat(parameters).isEmpty();
    }

    @Test
    @Disabled
    public void testParameters() {
        final MutationConfig config = createConfig().add("name", SIMPLE_NAME).add("param-test", "value").add("param-test2", "value");
        this.repository.registerTemplate(SIMPLE_NAME, SIMPLE_TRANSFORMATION);
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(1);
        final Mutation actual = mutations.get(0);
        assertThat((Object) actual.getConfiguration().getProperty(TransformationMutator.TEMPLATE_PARAM)).isNotNull();
        final Map<String, String> parameters = actual.getConfiguration().getProperty(TransformationMutator.PARAMETER_PARAM);
        assertThat(parameters).isNotNull();
        assertThat(parameters).hasSize(2);
    }

    @Test
    public void testMissingTemplate() {
        final MutationConfig config = createConfig().add("name", SIMPLE_NAME);
        assertThrows(MutationException.class, () -> {
            this.generator.generateMutations(config, createContext());
        });
    }

}
