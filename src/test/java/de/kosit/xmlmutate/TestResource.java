package de.kosit.xmlmutate;

import de.kosit.xmlmutate.runner.Services;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.validation.Schema;

/**
 * Static access to named resources for testing.
 *
 * @author Andreas Penski
 */
public class TestResource {

    public static class BookResources {

        public static final URI ROOT = TEST_ROOT.resolve("book/");

        public static final URI SCHEMA = ROOT.resolve("book.xsd");

        public static final URI XSL = ROOT.resolve("book.xsl");

        public static final URI SIMPLE = ROOT.resolve("book.xml");

        public static final URI NO_SCHEMATRON = ROOT.resolve("book_without_schematron.xml");

        public static final URI ORIGINAL_SCHEMA_INVALID = ROOT.resolve("book_original_invalid_schema.xml");

        public static final URI ACTION_ERROR_1ST_MUTATION = ROOT.resolve("book_action_error_1st_mutation.xml");

        public static final URI ACTION_ERROR_2ND_MUTATION = ROOT.resolve("book_action_error_2nd_mutation.xml");

        public static final URI PARSER_ERROR_1ST_MUTATION = ROOT.resolve("book_parsing_error_1st_mutation.xml");

        public static final URI PARSER_ERROR_2ND_MUTATION = ROOT.resolve("book_parsing_error_2nd_mutation.xml");

        public static final URI DUPLICATE_IDS = ROOT.resolve("book_duplicate_ids.xml");

        public static final URI WITH_XSLT_TRANSFORM = ROOT.resolve("with_xslt_transform.xml");

        public static final URI SCHEMATRON_BOOK1_FAILED = ROOT.resolve("book_with_failed_Book-1.xml");
        public static final URI SCHEMATRON_BOOK2_FAILED = ROOT.resolve("book_with_failed_Book-2.xml");
        public static final URI SCHEMATRON_BOOK2_PASSED = ROOT.resolve("book_with_passed_Book-2.xml");
        public static final URI SCHEMATRON_BOOK1_BOOK2_FAILED = ROOT.resolve("book_with_failed_Book-1_and_Book-2.xml");

        public static Schema getSchema() {
            return Services.getSchemaRepository().createSchema(SCHEMA);
        }
    }

    public static class UblResources {
        public static final URI ROOT = TEST_ROOT.resolve("ubl-en16931/");
        public static final URI XSL = ROOT.resolve("EN16931-UBL-validation.xsl");
        public static final URI XML = ROOT.resolve("schema-generated-instance.xml");
    }

    public static class TransformResource {

        public static final URI ROOT = TEST_ROOT.resolve("transform/");

        public static final URI SIMPLE_TRANSFORM = ROOT.resolve("simple.xsl");

        public static final URI INVALD_TRANSFORM = ROOT.resolve("invalid.xsl");

        public static final URI BOOK_XML = ROOT.resolve("simple_book.xml");

        public static final URI BOOK_XML_WITH_PARAM = ROOT.resolve("simple_book_with_param.xml");
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
        result.addAll(Stream.of(uris).map(TestResource::asPath).toList());
        return result;
    }

    public static Path asPath(final URI uri) {
        return Paths.get(uri);
    }
}
