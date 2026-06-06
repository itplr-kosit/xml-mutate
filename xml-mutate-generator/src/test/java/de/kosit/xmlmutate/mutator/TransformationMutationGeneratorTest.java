package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.TEST_ROOT;
import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.kosit.xmlmutate.TestResource.TransformResource;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.TemplateRepository;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the Generator for {@link TransformationMutator}.
 * 
 * @author Andreas Penski
 */
public class TransformationMutationGeneratorTest {

    private static final URI SIMPLE_TRANSFORMATION = TEST_ROOT.resolve("transform/simple.xsl");

    private static final String MUTATOR_NAME = "xslt";

    private static final String SIMPLE_NAME = "simple";

    private TemplateRepository repository;

    private TransformationMutationGenerator generator;

    @BeforeEach
    public void setup() {
        this.repository = new TemplateRepository();
        this.generator = new TransformationMutationGenerator(this.repository);
    }

    @Test
    public void testSimpleGenerate() {
        final MutationConfig config = createConfig().add("template", SIMPLE_NAME);
        config.setMutatorName(MUTATOR_NAME);
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
    public void testParameters() {
        final MutationConfig config = createConfig().add("template", SIMPLE_NAME).add("param-test", "value").add("param-test2", "value");
        config.setMutatorName(MUTATOR_NAME);
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
    public void testRelativeToDocumentTest() {
        final MutationConfig config = createConfig().add("template", "simple.xsl");
        config.setMutatorName(MUTATOR_NAME);
        final MutationDocumentContext context = createContext(d -> {
        }, Paths.get(TransformResource.BOOK_XML));
        final List<Mutation> mutations = this.generator.generateMutations(config, context);
        assertThat(mutations).hasSize(1);
        assertThat(this.repository.getTemplates()).hasSize(1);
    }

    @Test
    public void testRelativeToCwdTest() {
        final MutationConfig config = createConfig().add("template", "src/test/resources/transform/simple.xsl");
        config.setMutatorName(MUTATOR_NAME);
        final MutationDocumentContext context = createContext(d -> {
        }, Paths.get(TransformResource.BOOK_XML));
        final List<Mutation> mutations = this.generator.generateMutations(config, context);
        assertThat(mutations).hasSize(1);
        assertThat(this.repository.getTemplates()).hasSize(1);
    }

    @Test
    public void testMissingTemplate() {
        final MutationConfig config = createConfig().add("name", SIMPLE_NAME);
        config.setMutatorName(MUTATOR_NAME);
        assertThrows(MutationException.class,
            () -> this.generator.generateMutations(config, createContext()));
    }

}
