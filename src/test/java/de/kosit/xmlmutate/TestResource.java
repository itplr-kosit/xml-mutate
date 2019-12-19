package de.kosit.xmlmutate;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.validation.Schema;

import de.kosit.xmlmutate.runner.Services;

/**
 * Static access to named resources for testing.
 * 
 * @author Andreas Penski
 */
public class TestResource {

    public static class BookResources {

        public static final URI ROOT = TEST_ROOT.resolve("book/");

        public static final URI SCHEMA = ROOT.resolve("book.xsd");

        public static final URI SIMPLE = ROOT.resolve("book.xml");

        public static final URI NO_SCHEMATRON = ROOT.resolve("book_no_schematron.xml");

        public static final URI WITH_XSLT_TRANSFORM = ROOT.resolve("with_xslt_transform.xml");

        public static Schema getSchema() {
            return Services.getSchemaRepository().createSchema(SCHEMA);
        }
    }

    public static class TransformResource {

        public static final URI ROOT = TEST_ROOT.resolve("transform/");

        public static final URI SIMPLE_TRANSFORM = ROOT.resolve("simple.xsl");

        public static final URI INVALD_TRANSFORM = ROOT.resolve("invalid.xsl");

        public static final URI BOOK_XML = ROOT.resolve("simple_book.xml");
    }

    public static final URI TEST_ROOT = Paths.get("src/test/resources").toUri();

    public static final URI TEST_TARGET = Paths.get("target/test-target").toUri();

    /**
     * Converts an array of URI to path. There is no checking whether the uri is actually pointing to a valid path.
     * 
     * @param uris the uris
     * @return the List of path
     */
    public static List<Path> asPath(final URI uri, final URI... uris) {
        final List<Path> result = new ArrayList<>();
        result.add(asPath(uri));
        result.addAll(Stream.of(uris).map(TestResource::asPath).collect(Collectors.toList()));
        return result;
    }

    public static Path asPath(final URI uri) {
        return Paths.get(uri);
    }
}
