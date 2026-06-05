package de.kosit.commons.validate;

import static org.assertj.core.api.Assertions.assertThat;

import de.init.kosit.commons.validate.SchematronValidationService;
import de.kosit.xmlmutate.mutation.Schematron;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.s9api.XdmDestination;
import org.junit.jupiter.api.Test;

class SchematronValidationServiceTest {

  private static final URI TEST_ROOT = Paths.get("src/test/resources").toUri();
  private static final URI ROOT = TEST_ROOT.resolve("svrl/");
  private static final URI XSL = ROOT.resolve("document_schematron.xsl");
  private static final URI XML = ROOT.resolve("document_with_schematron_failures.xml");

  private static final String EXPECTED_XPATH_1 = "/Q{urn:oasis:names:specification:ubl:schema:xsd:ContractAwardNotice-2}ContractAwardNotice[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}UBLExtensions[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}UBLExtension[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}ExtensionContent[1]/Q{http://data.europa.eu/p27/eforms-ubl-extensions/1}EformsExtension[1]/Q{http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1}Organizations[1]/Q{http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1}UltimateBeneficialOwner[3]";
  private static final String EXPECTED_XPATH_2 = "/Q{urn:oasis:names:specification:ubl:schema:xsd:ContractAwardNotice-2}ContractAwardNotice[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}UBLExtensions[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}UBLExtension[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2}ExtensionContent[1]/Q{http://data.europa.eu/p27/eforms-ubl-extensions/1}EformsExtension[1]/Q{http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1}Organizations[1]/Q{http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1}UltimateBeneficialOwner[2]";
  private static final String SCHEMATRON_RULE_ID = "CR-DE-BT-513-UBO";
  private static final String SCHEMATRON_NAME = "testSchematron";

  private final SchematronValidationService service = new SchematronValidationService();

  @Test
  void shouldGenerateSvrlReport() {
    Schematron schematron = new Schematron(
        SCHEMATRON_NAME, XSL, List.of(SCHEMATRON_RULE_ID));

    XdmDestination result = service.validate(XML, schematron);

    assertThat(result).isNotNull();
    assertThat(result.getXdmNode()).isNotNull();
    assertThat(result.getXdmNode().toString()).contains("svrl:failed-assert");
  }

  @Test
  void shouldExtractErrorsWithXPathsFromSvrl() {
    Schematron schematron = new Schematron(
        SCHEMATRON_NAME, XSL, List.of(SCHEMATRON_RULE_ID));
    XdmDestination xdmDestination = service.validate(XML, schematron);

    Map<String, Set<String>> result = service.findFailuresWithXPaths(xdmDestination);

    assertThat(result)
        .isNotNull()
        .isNotEmpty()
        .hasSize(1)
        .containsKey(SCHEMATRON_RULE_ID);

    assertThat(result.get(SCHEMATRON_RULE_ID))
        .isNotNull()
        .isNotEmpty()
        .hasSize(2);

    assertThat(result.get(SCHEMATRON_RULE_ID))
        .contains(EXPECTED_XPATH_1, EXPECTED_XPATH_2);
  }

}
