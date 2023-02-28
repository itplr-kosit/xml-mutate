package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.createConfig;
import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.runner.MutationException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests the genericode based codelists with {@link CodeMutationGenerator}s.
 * 
 * @author Andreas Penski
 */
public class CodeMutationGeneratorGenericodeTest {

    private static final URI TEST_ROOT = Paths.get("src/test/resources").toUri();

    /**
     * "https://www.xrepository.de/api/xrepository/urn:de:xauslaender:codelist:geschlecht_2/genericode"
     */
    private static final String XREPOSITORY_DE_XAUSLAENDER_CODELIST_GESCHLECHT_2_GENERICODE =
        "src/test/resources/genericode/CL_XA_Geschlecht.xml";

    private final CodeMutationGenerator generator = new CodeMutationGenerator();

    @Test
    public void testSimpleGenericode() {
        final MutationConfig config = createConfig().add("genericode", TEST_ROOT.resolve("genericode/example1.xml"));
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(15);
    }

    @Test
    public void testGenericodeWithKey() {
        final MutationConfig config = createConfig();
        config.add("genericode", TEST_ROOT.resolve("genericode/example2.xml"));
        config.add("codeKey", "Schlüssel");
        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());
        assertThat(mutations).hasSize(4);
    }

    @Test
    public void testEmptyConfig() {
        final MutationConfig config = createConfig().add("genericode", "");
        assertThrows(MutationException.class, () ->
            this.generator.generateMutations(config, createContext()));
    }

    @Test
    public void testWrongCodeKey() {
        final MutationConfig config = createConfig();
        config.add("genericode", TEST_ROOT.resolve("genericode/geschlecht.xml"));
        config.add("codeKey", "code");
        assertThrows(MutationException.class, () ->
            this.generator.generateMutations(config, createContext()));
    }

    @Test
    public void testLoadRemoteCodeliste() {
        final MutationConfig config = createConfig();
        config.add("genericode", XREPOSITORY_DE_XAUSLAENDER_CODELIST_GESCHLECHT_2_GENERICODE);
        config.add("codeKey", "Schlüssel");

        final List<Mutation> mutations = this.generator.generateMutations(config, createContext());

        assertThat(mutations).hasSize(4);
    }

    @Test
    public void testLoadRelativeToDocument() {
        final MutationConfig config = createConfig();
        config.add("genericode", "genericode/example2.xml");
        config.add("codeKey", "Schlüssel");
        final MutationDocumentContext context = createContext(Paths.get("src/test/resources/Dummy.xml"));
        final List<Mutation> mutations = this.generator.generateMutations(config, context);
        assertThat(mutations).hasSize(4);
    }

    @Test
    public void testLoadRelativeToCurrentWorkingDirectory() {
        final MutationConfig config = createConfig();
        config.add("genericode", "src/test/resources/genericode/example2.xml");
        config.add("codeKey", "Schlüssel");
        final MutationDocumentContext context = createContext(Paths.get("src/test/resources/Dummy.xml"));
        final List<Mutation> mutations = this.generator.generateMutations(config, context);
        assertThat(mutations).hasSize(4);
    }

}
