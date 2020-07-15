package de.kosit.xmlmutate;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;

import de.kosit.xmlmutate.runner.SavingMode;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.runner.FailureMode;
import de.kosit.xmlmutate.runner.RunnerConfig;
import de.kosit.xmlmutate.runner.Services;

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

    private static final Path DOCUMENT_PATH = Paths.get("Dummy.xml");

    /**
     * Creates a simple mutation context for testing
     *
     * @return the context
     */
    public static MutationContext createContext(final Path documentPath) {
        return createContext(NOOP, documentPath);
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @return the context
     */
    public static MutationContext createContext() {
        return createContext(DOCUMENT_PATH);
    }

    /**
     * Create a mutation context with a give document for testing
     *
     * @param doc the document
     * @param documentPath the path
     * @return a dummy context
     */
    public static MutationContext createContext(final Document doc, final Path documentPath) {
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", "mutator=noop");
        return new MutationContext(pi, documentPath);
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param consumer a consumer for manipulating the target element
     * @return the context
     */
    public static MutationContext createContext(final Consumer<Element> consumer) {
        return createContext("mutator=noop", DOCUMENT_PATH, consumer);
    }

    public static MutationContext createContext(final Consumer<Element> consumer, final Path documentPath) {
        return createContext("mutator=noop", documentPath, consumer);
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param piString a configuration for the PI
     * @return the context
     */
    public static MutationContext createContext(final String piString) {
        return createContext(piString, DOCUMENT_PATH, d -> {
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
        return createContext(piString, DOCUMENT_PATH, consumer);
    }

    /**
     * Creates a simple mutation context for testing
     *
     * @param piString a configuration for the PI
     * @param consumer a consumer for manipulating the target element
     * @param documentPath the path to the root document
     * @return the context
     */
    public static MutationContext createContext(final String piString, final Path documentPath, final Consumer<Element> consumer) {
        final Document doc = de.init.kosit.commons.ObjectFactory.createDocumentBuilder(false).newDocument();
        final Element root = doc.createElement("root");
        doc.appendChild(root);
        final ProcessingInstruction pi = doc.createProcessingInstruction("xmute", piString);
        root.appendChild(pi);
        final Element target = doc.createElement("target");
        root.appendChild(target);
        consumer.accept(target);
        return new MutationContext(pi, documentPath, SavingMode.SINGLE);
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
        return new MutationContext(pi, Paths.get("Dummy.xml"));
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

    public static RunnerConfig createRunnerConfig(final URI documentPath) {
        return RunnerConfig.Builder.forDocuments(getSingleDocument(documentPath)).checkSchema(TestResource.BookResources.getSchema())
                .checkSchematron(getBookSchematronRules()).targetFolder(createTestTargetFolder("doc/test"))
                .useTransformations(new ArrayList<>()).withFailureMode(FailureMode.FAIL_AT_END).build();
    }

    public static RunnerConfig createRunnerConfig(final URI documentPath, final FailureMode failureMode) {
        final RunnerConfig runnerConfig = createRunnerConfig(documentPath);
        runnerConfig.setFailureMode(failureMode);
        return runnerConfig;
    }

    public static RunnerConfig createRunnerConfig(final URI documentPath, final boolean ignoreSchemainvalidity) {
        final RunnerConfig runnerConfig = createRunnerConfig(documentPath);
        runnerConfig.setIgnoreSchemaInvalidity(ignoreSchemainvalidity);
        return runnerConfig;
    }

    private static List<Path> getSingleDocument(final URI stringPath) {
        final Path path = Paths.get(stringPath);
        return Collections.singletonList(path);
    }


    private static List<Schematron> getBookSchematronRules() {
        final List<Schematron> schematronList = new ArrayList<>();
        final URI uri = TestResource.BookResources.XSL;
        // Only with BR-DE-1 and BR-DE-2 as known rule names
        final List<String> list = Arrays.asList("Book-1", "Book-2");
        final Schematron schematron = new Schematron("schematron", uri, list);
        schematronList.add(schematron);
        return schematronList;
    }

    private static Path createTestTargetFolder(final String path) {
        final Path testPath = Paths.get(path);
        try {
            Files.createDirectories(testPath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (Files.exists(testPath) && !Files.isWritable(testPath)) {
            throw new IllegalArgumentException("Target folder is not writable");
        }
        return testPath;
    }
}
