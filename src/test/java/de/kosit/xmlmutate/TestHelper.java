package de.kosit.xmlmutate;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.runner.FailureMode;
import de.kosit.xmlmutate.runner.RunnerConfig;
import de.kosit.xmlmutate.runner.Services;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Some helper function for testing.
 * 
 * @author Andreas Penski
 */
public class TestHelper {

    public static final String SRC_TEST_RESOURCES_STR = "src/test/resources/";

    public static final URI SRC_TEST_RESOURCES = URI.create(SRC_TEST_RESOURCES_STR);

    public static final URI TEST_ROOT = Paths.get(SRC_TEST_RESOURCES_STR).toUri();

    private static final Consumer<Element> NOOP = whatever -> {
    };

    /**
     * Creates a simple mutation context for testing
     * 
     * @return the context
     */
    public static MutationContext createContext() {
        return createContext(NOOP);
    }


    /**
     * Create a mutation context with a give document for testing
     * @param doc
     * @return
     */
    public static MutationContext createContext(final Document doc, final String documentName) {
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", "mutator=noop");
        return new MutationContext(pi, documentName);
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param consumer a consumer for manipulating the target element
     * @return the context
     */
    public static MutationContext createContext(final Consumer<Element> consumer) {
        return createContext("mutator=noop", consumer);
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param piString a configuration for the PI
     * @return the context
     */
    public static MutationContext createContext(final String piString) {
        return createContext(piString, d -> {
        });
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param piString a configuration for the PI
     * @param consumer a consumer for manipulating the target element
     * @return the context
     */
    public static MutationContext createContext(final String piString, final Consumer<Element> consumer) {
        final Document doc = de.init.kosit.commons.ObjectFactory.createDocumentBuilder(false).newDocument();
        final Element root = doc.createElement("root");
        doc.appendChild(root);
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", piString);
        root.appendChild(pi);
        final Element target = doc.createElement("target");
        root.appendChild(target);
        consumer.accept(target);
        return new MutationContext(pi, "test");
    }

    /**
     * Creates a context with PI on root node.
     * 
     * @return the context
     */
    public static MutationContext createRootContext() {
        return createRootContext("mutator=noop", NOOP);
    }

    /**
     * Creates a context with PI on root node.
     * 
     * @param piString the pi string
     * @return the context
     */
    public static MutationContext createRootContext(final String piString) {
        return createRootContext(piString, NOOP);
    }

    /**
     * Creates a context with PI on root node.
     * 
     * @param piString the pi string
     * @param consumer consumer for manipulating the target node
     * @return the context
     */
    public static MutationContext createRootContext(final String piString, final Consumer<Element> consumer) {
        final Document doc = de.init.kosit.commons.ObjectFactory.createDocumentBuilder(false).newDocument();
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", piString);
        doc.appendChild(pi);
        final Element root = doc.createElement("root");
        doc.appendChild(root);
        consumer.accept(root);
        return new MutationContext(pi, "test");
    }

    /**
     * Create an empty {@link MutationConfig}.
     * 
     * @return the config
     */
    public static MutationConfig createConfig() {
        return new MutationConfig();
    }

    /**
     * Create a {@link MutationConfig} with some initial properties.
     * 
     * @return the config
     */
    public static MutationConfig createConfig(final Map<String, Object> properties) {
        final MutationConfig config = createConfig();
        config.setProperties(properties);
        return config;
    }

    public static String serialize(final Document doc) {
        try ( final StringWriter writer = new StringWriter() ) {
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (final IOException | TransformerException e) {
            throw new IllegalStateException("Can not serialize document", e);
        }
    }

    public static Stream<Node> streamElements(final NodeList list) {
        return stream(list, Node.ELEMENT_NODE);
    }

    public static Stream<Node> stream(final NodeList list, final short... types) {
        return IntStream.range(0, list.getLength()).mapToObj(list::item).filter(n -> ArrayUtils.contains(types, n.getNodeType()));
    }


    public static RunnerConfig createRunnerConfig(final String documentPath) {
        return RunnerConfig.Builder
                .forDocuments(getSingleDocument(documentPath))
                .checkSchema(createBookSchema())
                .checkSchematron(getBookSchematronRules())
                .targetFolder(createTestTargetFolder("doc/test"))
                .useTransformations(new ArrayList<>())
                .withFailureMode(FailureMode.FAIL_AT_END)
                .build();
    }

    public static RunnerConfig createRunnerConfig(final String documentPath, final FailureMode failureMode) {
        final RunnerConfig runnerConfig = createRunnerConfig(documentPath);
        runnerConfig.setFailureMode(failureMode);
        return runnerConfig;
    }

    public static RunnerConfig createRunnerConfig(final String documentPath, final boolean ignoreSchemainvalidity) {
        final RunnerConfig runnerConfig = createRunnerConfig(documentPath);
        runnerConfig.setIgnoreSchemaInvalidity(ignoreSchemainvalidity);
        return runnerConfig;
    }


    private static List<Path> getSingleDocument(final String stringPath) {
        final Path path = Paths.get(stringPath);
        return Collections.singletonList(path);
    }

    private static Schema createBookSchema() {
        final URI uri = Paths.get("src/test/resources/book/book.xsd").toUri();
        return Services.getSchemaRepository().createSchema(uri);
    }

    private static List<Schematron> getBookSchematronRules() {
        final List<Schematron> schematronList = new ArrayList<>();
        final URI uri = Paths.get("src/test/resources/book/book.xsl").toUri();
        // Only with BR-DE-1 and BR-DE-2 as known rule names
        final List<String> list = Arrays.asList("Book-1", "Book-2");
        final Schematron schematron = new Schematron("schematron", uri, list);
        schematronList.add(schematron);
        return schematronList;
    }

    private static Path createTestTargetFolder(final String path) {
        Path testPath = Paths.get(path);
        try {
            Files.createDirectories(testPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Files.exists(testPath) && !Files.isWritable(testPath)) {
            throw new IllegalArgumentException("Target folder is not writable");
        }
        return testPath;
    }
}
