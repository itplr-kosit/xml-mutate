package de.kosit.xmlmutate.schematron;

import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import de.kosit.xmlmutate.TestResource;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationResult;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.runner.Services;
import de.kosit.xmlmutate.runner.ValidateAction;

/**
 * A test class for all relevant combinations concerning schematron rules being declared or not
 *
 * The schema validation is being ignored
 *
 * @author Victor del Campo
 */
public class SchematronValidationTest {

    @Test
    @DisplayName("Test with a DECLARED rule that is known, failed and failed-expected")
    public void testSimpleFailedAndExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_FAILED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        final SchematronRuleExpectation ruleExpectation = new SchematronRuleExpectation("test", "Book-2", ExpectedResult.FAIL);
        config.addExpectation(ruleExpectation);

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);
    }

    @Test
    @DisplayName("Test with a DECLARED rule that is known, failed but passed-expected")
    public void testSimpleFailedNotExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_FAILED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        config.addExpectation(new SchematronRuleExpectation("test", "Book-2", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);

    }

    @Test
    @DisplayName("Test with a DECLARED rule that is known, passed and passed-expected")
    public void testSimplePassedAndExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_PASSED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        config.addExpectation(new SchematronRuleExpectation("test", "Book-2", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);

    }

    @Test
    @DisplayName("Test with a DECLARED rule that is known, passed and failed-expected")
    public void testSimplePassedNotExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_PASSED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        config.addExpectation(new SchematronRuleExpectation("test", "Book-2", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);

    }

    @Test
    @DisplayName("Test with NOT-DECLARED rule that fails and a DECLARED rule known, passed and passed-expected ")
    public void testNonDeclaredFailedAndDeclaredPassedAndExp() {
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_FAILED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "Book-1", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);

    }

    @Test
    @DisplayName("Test with NOT-DECLARED rule that fails and a DECLARED rule known, passed and failed-expected ")
    public void testNonDeclaredFailedAndDeclaredPassedNotExp() {
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_FAILED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "Book-1", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);

    }

    @Test
    @DisplayName("Test with NOT-DECLARED rule that fails and a DECLARED rule known, failed and passed-expected ")
    public void testNonDeclaredFailedAndDeclaredFailedNotExp() {
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK1_BOOK2_FAILED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "Book-1", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);

    }

    @Test
    @DisplayName("Test with NOT-DECLARED rule that fails and a DECLARED rule known, failed and failed-expected ")
    public void testNonDeclaredFailedAndDeclaredFailedAndExp() {
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK1_BOOK2_FAILED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "Book-1", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);

    }

    @Test
    @DisplayName("Test with DECLARED rule that is unknown and expected failed")
    public void testUnknownDeclaredRuleFailedExp() {
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_PASSED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "Book-145", ExpectedResult.FAIL));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);

    }

    @Test
    @DisplayName("Test with DECLARED rule that is unknown and expected passed")
    public void testUnknownDeclaredRulePassedExp() {
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_PASSED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.addExpectation(new SchematronRuleExpectation("test", "Book-145", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);

    }

    private ValidateAction createValidationAction() {
        final List<Schematron> schematronFiles = new ArrayList<>();
        final Path targetFolder = Paths.get(TestResource.TEST_ROOT);
        final Schema schema = TestResource.BookResources.getSchema();

        // Only extracted rule names BR-DE-1 and BR-DE-2 for this specific testing
        final Schematron schematron = new Schematron("schematron file", TestResource.BookResources.XSL, Arrays.asList("Book-1", "Book-2"));
        schematronFiles.add(schematron);
        return new ValidateAction(schema, schematronFiles, targetFolder);
    }

    private Document getXmlDocument(final Path xmlFile) {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile.toFile());
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
}
