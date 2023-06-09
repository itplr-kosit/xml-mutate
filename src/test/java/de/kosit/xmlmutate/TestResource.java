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

        public static final URI ACTION_ERROR_3RD_MUTATION = ROOT.resolve("book_action_error_3rd_mutation.xml");

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

    public static class EformWrongRuleResources {
        public static final URI ROOT = TEST_ROOT.resolve("eform-wrong-rule/");
        public static final URI XSL_EFORM = ROOT.resolve("eform-sch-rules.xsl");
        public static final URI XML_WITH_WRONG_SCH_RULE_REF =
            ROOT.resolve("eform-with-wrong-sch-rule-ref.xml");
    }

    public static class EformWithSchematronFailures {
        public static final URI ROOT = TEST_ROOT.resolve("eform-with-schematron-failures/");
        public static final URI XSL_EFORM = ROOT.resolve("eform-sch-rules.xsl");
        public static final URI XML_WITH_FAILING_SCHEMATRON_RULES =
            ROOT.resolve("eform-with-sch-failures.xml");
    }

    public static class EformWithSchematronRuleFailureAndTheRuleInMutator {
        public static final URI ROOT = TEST_ROOT.resolve("eform-with-failing-sch-rule-and-mutator-with-the-rule/");
        public static final URI XSL = ROOT.resolve("can_eforms_de.xsl");
        public static final URI XML = ROOT.resolve("can_eform_multiple_ubo.xml");
    }

    public static class EformWithAlternativeMutator {
        public static final URI ROOT = TEST_ROOT.resolve("eform-with-alternative-mutator/");
        public static final URI XSL = ROOT.resolve("eforms-de-schematron-validator-0.5.0.xsl");
        public static final URI XML = ROOT.resolve("eforms_CAN_E4_max-DE_valid_BT-556.xml");
    }

    public static class UblResources {
        public static final URI ROOT = TEST_ROOT.resolve("ubl-en16931/");
        public static final URI XSL = ROOT.resolve("EN16931-UBL-validation.xsl");
        public static final URI XML = ROOT.resolve("schema-generated-instance.xml");
    }

    public static class E2ESchematronWithShadowedRules {
        public static final URI ROOT = TEST_ROOT.resolve("e2e/shadowed/");
        public static final URI XSL_SCHEMATRON = ROOT.resolve("schematron-with-shadowed-rules.xsl");
        public static final URI XML_HAPPY_PATH =
            ROOT.resolve("xml-happy-path.xml");
        public static final URI XML_MAGAZINE_FAILURE_SHADOWED =
            ROOT.resolve("xml-magazine-failure-shadowed.xml");
        public static final URI XML_MAGAZINE_FAILURE_SHADOWED_INVALID =
            ROOT.resolve("xml-magazine-failure-shadowed-invalid.xml");
        public static final URI XML_MULTIPLE_XPATHS_ONE_FAILS =
            ROOT.resolve("xml-multiple-xpats-one-fails.xml");
        public static final URI XML =
            ROOT.resolve("xml-experiencing-shadowed-rules.xml");
    }

    public static class E2ESchematronWithoutAnyShadowedRules {
        public static final URI ROOT = TEST_ROOT.resolve("e2e/no-shadowed/");
        public static final URI XSL_SCHEMATRON = ROOT.resolve("schematron-rules.xsl");
        public static final URI XML_SHADOWING_SOLVED = ROOT.resolve("xml-shadowing-solved.xml");
        public static final URI XML_WITH_INVALID_MUTATOR = ROOT.resolve("xml-with-invalid-mutator.xml");
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
