package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static de.kosit.xmlmutate.assertions.Assertions.assertThat;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Tests {@link TransformationMutator}.
 * 
 * @author Andreas Penski
 */
public class TransformationMutatorTest {

    private final TransformationMutator mutator = new TransformationMutator();

    @Test
    public void simple() throws Exception {
        final String rand = randomAlphanumeric(6);
        final MutationContext context = createContext(e -> {
            e.setTextContent(rand);
        });
        final MutationConfig config = createConfig().add(TransformationMutator.TEMPLATE_PARAM, loadTemplate());
        this.mutator.mutate(context, config);
        assertThat(context.getTarget()).hasNodeName("test");
        assertThat(context.getTarget().getTextContent()).isNotEqualTo(rand);
        assertThat(context.getTarget()).containsText(rand);
    }

    @Test
    public void simpleWithParameter() throws Exception {
        final String rand = randomAlphanumeric(6);
        final MutationContext context = createContext(e -> {
            e.setTextContent(rand);
        });
        final Map<String, String> parameters = new HashedMap<>();
        final String randParamValue = randomAlphanumeric(6);
        parameters.put("simple-param", randParamValue);
        final MutationConfig config = createConfig().add(TransformationMutator.TEMPLATE_PARAM, loadTemplate())
                .add(TransformationMutator.PARAMETER_PARAM, parameters);
        this.mutator.mutate(context, config);
        assertThat(context.getTarget()).hasNodeName("test");
        assertThat(context.getTarget().getTextContent()).isNotEqualTo(rand);
        assertThat(context.getTarget()).containsText(rand);
        assertThat(context.getTarget()).containsText(randParamValue);
    }

    @Test
    public void simpleWithUnknownParameter() throws Exception {
        final String rand = randomAlphanumeric(6);
        final MutationContext context = createContext(e -> {
            e.setTextContent(rand);
        });
        final Map<String, String> parameters = new HashedMap<>();
        final String randParamValue = randomAlphanumeric(6);
        parameters.put("unknown", randParamValue);
        final MutationConfig config = createConfig().add(TransformationMutator.TEMPLATE_PARAM, loadTemplate())
                .add(TransformationMutator.PARAMETER_PARAM, parameters);
        this.mutator.mutate(context, config);
        assertThat(context.getTarget()).hasNodeName("test");
        assertThat(context.getTarget().getTextContent()).isNotEqualTo(rand);
        assertThat(context.getTarget()).containsText(rand);
    }

    @Test
    public void simpleWithMissingRequiredParameter() throws Exception {
        final String rand = randomAlphanumeric(6);
        final MutationContext context = createContext(e -> {
            e.setTextContent(rand);
        });
        final MutationConfig config = createConfig().add(TransformationMutator.TEMPLATE_PARAM, loadTemplate(Objects
                .requireNonNull(TransformationMutatorTest.class.getClassLoader().getResource("transform/simpleWithRequiredParam.xsl"))));
        assertThrows(MutationException.class, () -> {
            this.mutator.mutate(context, config);

        });

    }

    private Templates loadTemplate() throws IOException, TransformerConfigurationException {
        return loadTemplate(Objects.requireNonNull(TransformationMutatorTest.class.getClassLoader().getResource("transform/simple.xsl")));
    }

    private Templates loadTemplate(final URL url) throws IOException, TransformerConfigurationException {
        final TransformerFactory factory = TransformerFactory.newInstance();
        return factory.newTemplates(new StreamSource(url.openStream()));
    }
}
