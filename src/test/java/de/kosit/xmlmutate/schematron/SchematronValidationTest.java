package de.kosit.xmlmutate.schematron;


import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationResult;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.runner.Services;
import de.kosit.xmlmutate.runner.ValidateAction;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A test class for all relevant combinations concerning schematron rules being declared or not
 *
 * The schema validation is being ignored
 *
 * @author Victor del Campo
 */
public class SchematronValidationTest {


    private static final String TEST_RESOURCES_FOLDER = "src/test/resources";

    private static final Path SCHEMA_FILE = Paths.get(TEST_RESOURCES_FOLDER, "/xsd/maindoc/UBL-Invoice-2.1.xsd");
    private static final Path SCHEMATRON_FILE = Paths.get(TEST_RESOURCES_FOLDER, "/XRechnung-UBL-validation-Invoice.xsl");

    @Test
    @DisplayName("Test with a DECLARED rule that is known, failed and failed-expected")
    public void testSimpleFailedAndExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final String documentName = "Test_with_failed_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-2", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isTrue();
    }

    @Test
    @DisplayName("Test with a DECLARED rule that is known, failed but passed-expected")
    public void testSimpleFailedNotExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final String documentName = "Test_with_failed_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-2", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isFalse();
    }

    @Test
    @DisplayName("Test with a DECLARED rule that is known, passed and passed-expected")
    public void testSimplePassedAndExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final String documentName = "Test_with_passed_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-2", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isTrue();
    }

    @Test
    @DisplayName("Test with a DECLARED rule that is known, passed and failed-expected")
    public void testSimplePassedNotExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final String documentName = "Test_with_passed_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-2", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isFalse();
    }

    @Test
    @DisplayName("Test with NOT-DECLARED rule that fails and a DECLARED rule known, passed and passed-expected ")
    public void testNonDeclaredFailedAndDeclaredPassedAndExp() {
        final String documentName = "Test_with_failed_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-1", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isTrue();
    }

    @Test
    @DisplayName("Test with NOT-DECLARED rule that fails and a DECLARED rule known, passed and failed-expected ")
    public void testNonDeclaredFailedAndDeclaredPassedNotExp() {
        final String documentName = "Test_with_failed_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-1", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isFalse();
    }

    @Test
    @DisplayName("Test with NOT-DECLARED rule that fails and a DECLARED rule known, failed and passed-expected ")
    public void testNonDeclaredFailedAndDeclaredFailedNotExp() {
        final String documentName = "Test_with_failed_BR-DE-1_and_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-1", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isFalse();
    }

    @Test
    @DisplayName("Test with NOT-DECLARED rule that fails and a DECLARED rule known, failed and failed-expected ")
    public void testNonDeclaredFailedAndDeclaredFailedAndExp() {
        final String documentName = "Test_with_failed_BR-DE-1_and_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-1", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isTrue();
    }


    @Test
    @DisplayName("Test with DECLARED rule that is unknown and expected failed")
    public void testUnknownDeclaredRuleFailedExp() {
        final String documentName = "Test_with_passed_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-145", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isTrue();
    }

    @Test
    @DisplayName("Test with DECLARED rule that is unknown and expected passed")
    public void testUnknownDeclaredRulePassedExp() {
        final String documentName = "Test_with_passed_BR-DE-2.xml";
        final Document doc = getXmlDocument(Paths.get(TEST_RESOURCES_FOLDER, "validation/", documentName).toString());

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "BR-DE-145", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, documentName), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);
        assertThat(mutation.getResult().isSchematronGlobalValidationAsExpected()).isFalse();
    }


    private ValidateAction createValidationAction() {
        final List<Schematron> schematronFiles = new ArrayList<>();
        final Path targetFolder = Paths.get(TEST_RESOURCES_FOLDER);
        final Schema schema = Services.getSchemaRepository().createSchema(SCHEMA_FILE.toUri());

        // Only extracted rule names BR-DE-1 and BR-DE-2 for this specific testing
        final Schematron schematron = new Schematron("schematron file", SCHEMATRON_FILE.toUri(), Arrays.asList("BR-DE-1", "BR-DE-2"));
        schematronFiles.add(schematron);
        return new ValidateAction(schema, schematronFiles, targetFolder);
    }

    private Document getXmlDocument(final String xmlFile) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
}
