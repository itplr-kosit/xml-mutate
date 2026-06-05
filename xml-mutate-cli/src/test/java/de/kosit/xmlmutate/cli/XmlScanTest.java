package de.kosit.xmlmutate.cli;

import static org.assertj.core.api.Assertions.assertThat;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.TestResource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class XmlScanTest {

  private XmlScan xmlScan;
  private XmlMutate xmlMutate;
  private final URI uri = TestResource.BookResources.SCAN;
  private final Path documentPath = Paths.get(uri);
  ExecutorService executorService;

  @BeforeEach
  void setUp() {
    xmlScan = new XmlScan();
    xmlMutate = new XmlMutate();
    xmlScan.xmlMutate = xmlMutate;
    executorService = Executors.newFixedThreadPool(1);
  }

  @AfterEach
  void tearDown() {
    executorService.shutdown();
  }

  @Test
  void testProcess_Scan() {
    xmlMutate.documents = List.of(documentPath);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    xmlScan.process(executorService);

    System.setOut(System.out);

    String output = outContent.toString();
    assertThat(output).contains("File: " + documentPath);
  }

  @Test
  @DisplayName("Should scan not existing document and print error")
  void testProcess_ScanWithInvalidDocument() {
    xmlMutate.documents = Arrays.asList(TestHelper.DOCUMENT_PATH);

    IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
        () -> {
          xmlScan.process(executorService);
        });

    assertThat(exception.getMessage()).contains(
        "Document " + TestHelper.DOCUMENT_PATH + " does not exist or is not readable");
  }

  @Test
  @DisplayName("Should scan one valid and one invalid document and print error")
  void testProcess_ScanOneValidOneInvalidDocument() {
    List<Path> documents = new ArrayList<>();
    documents.add(documentPath);
    documents.add(TestHelper.DOCUMENT_PATH);
    xmlMutate.documents = documents;

    IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
        () -> {
          xmlScan.process(executorService);
        });

    assertThat(exception.getMessage()).contains(
        "Document " + TestHelper.DOCUMENT_PATH + " does not exist or is not readable");
  }

  @Test
  @DisplayName("Should scan document and print two xmute elements in result")
  void testProcessDocument_Scan() {
    StringBuilder result = xmlScan.processDocument(documentPath);
    String expectedFile = "File: " + documentPath;
    String expectedRule = "Rule: efde:CL-DE-BT-11";
    String expectedValid = "Expectations: valid";
    String expectedInvalid = "Expectations: invalid";
    String expectedMutation = "Mutation: code";
    String expectedValuesValid = "Values: koerp-oer-bund, anst-oer-bund, stift-oer-bund, koerp-oer-kommun, anst-oer-kommun, stift-oer-kommun, koerp-oer-land, anst-oer-land, stift-oer-land, oberst-bbeh, omu-bbeh-niedrig, omu-bbeh, def-cont, eu-ins-bod-ag, grp-p-aut, int-org, kommun-beh, org-sub, pub-undert, pub-undert-cga, pub-undert-la, pub-undert-ra, oberst-lbeh, omu-lbeh, spec-rights-entity";
    String expectedValuesInvalid = "Values: not-valid-code, kbeh";

    assertThat(result.toString()).contains(expectedFile);
    assertThat(result.toString()).contains(expectedRule);
    assertThat(result.toString()).contains(expectedValid);
    assertThat(result.toString()).contains(expectedInvalid);
    assertThat(result.toString()).contains(expectedMutation);
    assertThat(result.toString()).contains(expectedValuesValid);
    assertThat(result.toString()).contains(expectedValuesInvalid);
  }

  @Test
  @DisplayName("Should scan document and print snippets")
  void testProcessDocument_ScanWithSnippets() {
    xmlScan.snippets = true;
    StringBuilder result = xmlScan.processDocument(documentPath);
    String expectedSnippet =
        "<cbc:PartyTypeCode xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\"\n"
            + "                   listName=\"buyer-legal-type\">def-cont</cbc:PartyTypeCode>";

    assertThat(result.toString()).contains(expectedSnippet);
  }

}
