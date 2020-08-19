package de.kosit.xmlmutate.mutator;

import static de.kosit.xmlmutate.TestHelper.SRC_TEST_RESOURCES;
import static de.kosit.xmlmutate.TestHelper.TEST_ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutator.GeneriCodeMutationGenerator.Code;
import de.kosit.xmlmutate.mutator.GeneriCodeMutationGenerator.CodeFactory;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * It tests the generic-code parsing functionalities
 *
 * @author Andreas Penski
 */
public class GeneriCodeFactoryTest {

    public static final URI REMOTE_URI = URI.create(
            "https://www.xrepository.de/api/xrepository/urn:de:xauslaender:codelist:geschlecht_2:technischerBestandteilGenericode");

    @Test
    public void simpleParseTest() {
        final List<Code> result = CodeFactory.resolveCodes(TEST_ROOT.resolve("genericode/example2.xml"), "Schlüssel");
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(4);
    }

    @Test
    public void testWrongKey() {
        assertThrows(MutationException.class,
                () -> CodeFactory.resolveCodes(TEST_ROOT.resolve("genericode/example2.xml"), "doesNotExist"));
    }

    @Test
    @Disabled
    public void testParseRemote() {
        final List<Code> result = CodeFactory.resolveCodes(REMOTE_URI, "Schlüssel");
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(4);
    }

    @Test
    public void testLocal() {
        final List<Code> result = CodeFactory.resolveCodes(SRC_TEST_RESOURCES.resolve("genericode/example2.xml"),
                "Schlüssel");
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(4);
    }

    @Test
    public void testUnexisting() {
        assertThrows(MutationException.class, () -> {
            CodeFactory.resolveCodes(TEST_ROOT.resolve("doesNotExist.xml"));
        });
    }

    @Test
    public void testNullInput() {
        assertThrows(MutationException.class, () -> {
            CodeFactory.resolveCodes(null);
        });
    }

    @Test
    public void testWithoutKey() {
        final List<Code> result = CodeFactory.resolveCodes(TEST_ROOT.resolve("genericode/example1.xml"));
        assertThat(result).isNotEmpty();
    }

}
