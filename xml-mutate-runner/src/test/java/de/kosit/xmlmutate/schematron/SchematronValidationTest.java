package de.kosit.xmlmutate.schematron;

import static de.kosit.xmlmutate.TestHelper.createContext;
import static org.assertj.core.api.Assertions.assertThat;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.TestResource;
import de.kosit.xmlmutate.TestResource.EformWithSchematronFailures;
import de.kosit.xmlmutate.TestResource.EformWithSchematronRuleFailureAndTheRuleInMutator;
import de.kosit.xmlmutate.TestResource.EformWrongRuleResources;
import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronEnterity;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationResult;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.runner.FailureMode;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.MutationRunner;
import de.kosit.xmlmutate.runner.RunnerConfig;
import de.kosit.xmlmutate.runner.RunnerResult;
import de.kosit.xmlmutate.runner.ValidateAction;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A test class for all relevant combinations concerning schematron rules being declared or not
 * <p>
 * The schema validation is being ignored
 *
 * @author Victor del Campo
 */
public class SchematronValidationTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Test
    @DisplayName("Original XML has schematron rule failures. There is mutator which is specifying"
        + " the same schematron rule - that is failing in original XML. But in mutator this rule"
        + " actually passes and result for the mutator might be expected as SUCCESS. As the original"
        + " XML document without any mutators applied already had assertion failures with the same"
        + " schematron rule as defined in mutator - the result for mutation should "
        + " be marked as FAILED.")
    void specificMutationSchematronAssertionShouldFailAsOriginalDocumentHasExactRuleFailuresNotRelatedToTheMutation() {
        final URI testXmlResource = EformWithSchematronRuleFailureAndTheRuleInMutator.XML;
        List<Schematron> knownSchematronRules = getSchematronXslWithKnownUboRules();
        final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
            knownSchematronRules, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        RunnerResult result = runner.run();

        List<Pair<Path, List<Mutation>>> resultList = result.getResult();

        assertThat(resultList).isNotNull().hasSize(1);
        assertThat(resultList.get(0)).isNotNull();
        assertThat(resultList.get(0).getValue()).isNotNull().hasSize(1);
        assertThat(resultList.get(0).getValue().get(0)).isNotNull();
        assertThat(resultList.get(0).getValue().get(0).getContext()).isNotNull();
        assertThat(resultList.get(0).getValue().get(0).getContext().getSchematronFailures())
            .isNotNull()
            .hasSize(12)
            .containsKey("CR-DE-BT-513-UBO");
        assertThat(resultList.get(0).getValue().get(0).getMutationErrorContainer()).isNotNull();
        assertThat(resultList.get(0).getValue().get(0).getMutationErrorContainer().getAllErrorMessages())
            .isNotNull()
            .isNotEmpty();
    }

    @Test
    @DisplayName("It is expected to log details if original XML document has "
        + "any schematron errors without any mutators applied.")
    public void shouldWarnRegardingFailingSchematronAssertOnOriginalXmlDocument() {
        final URI testXmlResource = EformWithSchematronFailures.XML_WITH_FAILING_SCHEMATRON_RULES;
        List<Schematron> knownSchematronRules = getSchematronXslWithKnownRules();
        final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
            knownSchematronRules, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        RunnerResult result = runner.run();
        assertThat(result).isNotNull();
        List<Pair<Path, List<Mutation>>> resultList = result.getResult();

        assertThat(resultList)
            .isNotNull()
            .hasSize(1);
        assertThat(resultList.get(0)).isNotNull();
        assertThat(resultList.get(0).getValue())
            .isNotNull()
            .hasSize(1);
        assertThat(resultList.get(0).getValue().get(0))
            .isNotNull();
        assertThat(resultList.get(0).getValue().get(0).getContext())
            .isNotNull();
        assertThat(resultList.get(0).getValue().get(0).getContext().getSchematronFailures())
            .isNotNull()
            .isNotEmpty()
            .hasSize(1);
        assertThat(resultList.get(0).getValue().get(0).getContext().getSchematronFailures())
            .containsEntry("CR-DE-BT-510-Organization-Company",
                Set.of("/Q{urn:oasis:names:specification:ubl:schema:xsd:ContractAwardNotice-2}ContractAwardNotice[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}UBLExtensions[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}UBLExtension[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}ExtensionContent[1]/Q{http://data.europa.eu/p27/eforms-ubl-extensions/1}EformsExtension[1]/Q{http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1}Organizations[1]/Q{http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1}Organization[2]/Q{http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1}Company[1]"));
        assertThat(resultList.get(0).getValue().get(0).getMutationErrorContainer().getAllErrorMessages())
            .containsExactly("Failed rule CR-DE-BT-510-UBO instruction CR-DE-BT-510-UBO-remove");
    }

    @Test
    @DisplayName("Assuming that XML without any mutators applied is correct and schematron rule assertion"
        + "fails that is not defined by specific mutator - then unexpected assertion failure must be"
        + "explicitly logged.")
    public void shouldWarnRegardingFailingSchematronAssertWhenItIsNotExpected() {
        final URI testXmlResource = TestResource.EformWrongRuleResources.XML_WITH_WRONG_SCH_RULE_REF;
        List<Schematron> knownSchematronRules = getEformTransformedToXslSchematron_BT510_UBO_Rules();
        final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
            knownSchematronRules, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        RunnerResult result = runner.run();
        assertThat(result).isNotNull();
        List<Pair<Path, List<Mutation>>> resultList = result.getResult();

        assertThat(resultList).isNotNull();
        assertThat(resultList.get(0)).isNotNull();
        assertThat(resultList.get(0).getValue()).isNotNull().hasSize(1);
        assertThat(resultList.get(0).getValue().get(0)).isNotNull();
        assertThat(resultList.get(0).getValue().get(0).getContext()).isNotNull();
        assertThat(resultList.get(0).getValue().get(0).getContext().getSchematronFailures())
            .isNotNull()
            .isEmpty();
        assertThat(resultList.get(0).getValue().get(0).getMutationErrorContainer()).isNotNull();
        assertThat(resultList.get(0).getValue().get(0).getContext().getSchematronFailures()).isEmpty();
        assertThat(resultList.get(0).getValue().get(0).getMutationErrorContainer().getAllErrorMessages())
            .isNotNull()
            .isNotEmpty()
            .contains("Failed rule CR-DE-BT-510-UBO instruction CR-DE-BT-510-UBO-remove");
    }

    private List<Schematron> getEformTransformedToXslSchematron_BT510_UBO_Rules() {
        final List<Schematron> schematronList = new ArrayList<>();
        final URI uri = EformWrongRuleResources.XSL_EFORM;
        final List<String> list = Arrays.asList("CR-DE-BT-510-UBO", "CR-DE-BT-510-Organization-Company");
        final Schematron schematron = new Schematron("efde", uri, list);
        schematronList.add(schematron);
        return schematronList;
    }

    private List<Schematron> getSchematronXslWithKnownRules() {
        final List<Schematron> schematronList = new ArrayList<>();
        final URI uri = EformWithSchematronFailures.XSL_EFORM;
        final List<String> list = Arrays.asList("CR-DE-BT-510-UBO", "CR-DE-BT-510-Organization-Company");
        final Schematron schematron = new Schematron("efde", uri, list);
        schematronList.add(schematron);
        return schematronList;
    }

    private List<Schematron> getSchematronXslWithKnownUboRules() {
        final List<Schematron> schematronList = new ArrayList<>();
        final URI uri = EformWithSchematronFailures.XSL_EFORM;
        final List<String> list = Arrays.asList("CR-DE-BT-513-UBO", "CR-DE-BT-510-UBO");
        final Schematron schematron = new Schematron("efde", uri, list);
        schematronList.add(schematron);
        return schematronList;
    }

    @Test
    @DisplayName("XSL rule evaluating with runtime exception should evaluate mutator to failed status with global error message")
    public void shouldParseXmuteEmptyOnTargetAlreadyHavingNoValue() {
        final URI uri = TestResource.UblResources.XML;
        final List<Schematron> schematronRules = getUblInvoiceAllowanceChargeAmountSchematronRules();
        final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(uri,
            schematronRules, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        final RunnerResult result = runner.run();

        assertThat(result).isNotNull();
        assertThat(result.getResult()).isNotNull();
        assertThat(result.getResult()).hasSize(1);
        final List<Mutation> mutationResult = result.getResult().get(0).getValue();
        assertThat(mutationResult).isNotNull().hasSize(1);
        assertThat(mutationResult.get(0)).isNotNull();
        assertThat(mutationResult.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutationResult.get(0).getState()).isEqualTo(State.ERROR);
        assertThat(mutationResult.get(0).getResult()).isNotNull();
        assertThat(mutationResult.get(0).getResult().getSchematronValidationState()).isEqualTo(
            ValidationState.UNPROCESSED);
        assertThat(mutationResult.get(0).getResult().getSchematronExpectationMatches()).isEmpty();

        assertThat(mutationResult.get(0).getMutationErrorContainer()).isNotNull();
        final List<Exception> mutationResultErrorMessages = mutationResult.get(0).getMutationErrorContainer().getGlobalErrorMessages();
        assertThat(mutationResultErrorMessages).isNotNull().hasSize(1);
        assertThat(mutationResultErrorMessages.get(0)).isNotNull();
        assertThat(mutationResultErrorMessages.get(0).getMessage()).isEqualTo("Structural mismatch: Nothing found to empty");
        assertThat(mutationResultErrorMessages.get(0)).isInstanceOf(
            MutationException.class);
        assertThat(((MutationException) mutationResultErrorMessages.get(0)).getErrors()).isNotEmpty();
        assertThat(((MutationException) mutationResultErrorMessages.get(0)).getErrors()).hasSize(1);
    }

    private List<Schematron> getUblInvoiceAllowanceChargeAmountSchematronRules() {
        final List<Schematron> schematronList = new ArrayList<>();
        final URI uri = TestResource.UblResources.XSL;
        final List<String> list = Arrays.asList("BR-41", "BR-42");
        final Schematron schematron = new Schematron("schematron", uri, list);
        schematronList.add(schematron);
        return schematronList;
    }

    @Test
    @DisplayName("Test with a DECLARED rule that is known, failed and failed-expected")
    public void testSimpleFailedAndExpected() {
        // FAILED OR SUCCESS depending on xml file content
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_FAILED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        // DECLARATION (known/unknown) AND EXPECTATION
        final SchematronRuleExpectation ruleExpectation = new SchematronRuleExpectation("schematron", "Book-2", ExpectedResult.FAIL);
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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-2", ExpectedResult.PASS));

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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-2", ExpectedResult.PASS));

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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-2", ExpectedResult.FAIL));

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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-1", ExpectedResult.PASS));

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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-1", ExpectedResult.FAIL));

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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-1", ExpectedResult.PASS));

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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-1", ExpectedResult.FAIL));

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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-145", ExpectedResult.FAIL));

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
        config.addExpectation(new SchematronRuleExpectation("schematron", "Book-145", ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);

    }

    @Test
    @DisplayName("Test with schematron all valid but one schematron rule failes")
    public void testSchematronAllValidButOneFailed() {
        // FAILED OR SUCCESS depending on xml file content
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_FAILED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.setSchematronEnterityExpectation(Pair.of(SchematronEnterity.ALL, ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.INVALID);
    }

    @Test
    @DisplayName("Test with schematron all valid and no rules failed")
    public void testSchematronAllValidNoneFailed() {
        // FAILED OR SUCCESS depending on xml file content
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_PASSED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.setSchematronEnterityExpectation(Pair.of(SchematronEnterity.ALL, ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);
    }

    @Test
    @DisplayName("Test with schematron none valid and no rules failed")
    public void testSchematronNoneValidButAllPassed() {
        // FAILED OR SUCCESS depending on xml file content
        final Path filePath = Paths.get(TestResource.BookResources.SCHEMATRON_BOOK2_PASSED);
        final Document doc = getXmlDocument(filePath);

        final MutationConfig config = new MutationConfig();
        config.setSchematronEnterityExpectation(Pair.of(SchematronEnterity.NONE, ExpectedResult.PASS));

        final Mutation mutation = new Mutation(createContext(doc, filePath), RandomStringUtils.randomAlphanumeric(5), config);
        createValidationAction().run(mutation);

        assertThat(mutation.getResult().getSchematronValidationState()).isEqualTo(MutationResult.ValidationState.VALID);
    }

    private ValidateAction createValidationAction() {
        final List<Schematron> schematronFiles = new ArrayList<>();
        final Path targetFolder = Paths.get(TestResource.TEST_ROOT);
        final Schema schema = TestResource.BookResources.getSchema();

        // Only extracted rule names BR-DE-1 and BR-DE-2 for this specific testing
        final Schematron schematron = new Schematron("schematron", TestResource.BookResources.XSL, Arrays.asList("Book-1", "Book-2"));
        schematronFiles.add(schematron);
        return new ValidateAction(schema, schematronFiles, targetFolder);
    }

    private Document getXmlDocument(final Path xmlFile) {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
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
