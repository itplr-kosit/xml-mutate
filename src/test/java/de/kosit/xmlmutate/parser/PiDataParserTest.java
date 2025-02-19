package de.kosit.xmlmutate.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.kosit.xmlmutate.observation.OperationStatus;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * MutatorParserTest
 *
 * @author Renzo Kottmann
 */

class PiDataParserTest {

  private final String mutatorEntry = "mutator=\"hi\"";
  private PiDataParser p = null;
  private Map<String, String> dataEntries = null;

  @BeforeEach
  void newParser() {
    this.p = new PiDataParser();
    this.dataEntries = null;
  }

  @Test
  @DisplayName("Return empty map if empty pi data")
  void emptyPIData() {
    dataEntries = p.parsePiData("");
    assertTrue(dataEntries.isEmpty());
    assertTrue(p.hasError());
    assertFalse(p.getParsingStatus().hasMultiStatus());
    assertEquals(OperationStatus.ERROR, p.getParsingStatus().getSeverity());
  }

  @Test
  @DisplayName("Return empty map if null pi data")
  void noPIData() {
    Map<String, String> dataEntries = p.parsePiData(null);
    assertTrue(dataEntries.isEmpty());
    assertTrue(p.hasError());
    assertFalse(p.getParsingStatus().hasMultiStatus());
  }

  @Test
  @DisplayName("Check non-null and non-empty value for a key")
  void nonNullAndNonEmptyValue() {
    Map<String, String> dataEntries = p.parsePiData(this.mutatorEntry);
    assertTrue(dataEntries.containsKey("mutator"));
    assertNotNull(dataEntries.get("mutator"));
    assertFalse(dataEntries.get("mutator").isEmpty());
  }

  @Test
  @DisplayName("Get result on minimum correct pi data")
  void haveResultOnMinimalCorrectInput() {
    Map<String, String> dataEntries = p.parsePiData(this.mutatorEntry);
    assertFalse(dataEntries.isEmpty());
    assertTrue(dataEntries.containsKey("mutator"));
  }

  @Test
  @DisplayName("Allow spaces and tabs between key and value")
  void allowSpaceBetweenKeyAndValue() {
    Map<String, String> dataEntries = p.parsePiData("mutator \t   \t\t=\"with spaces\"");
    assertTrue(dataEntries.containsKey("mutator"));
  }

  @Test
  @DisplayName("Allow several keys")
  void moreKeys() {
    dataEntries = p.parsePiData("mutator=\"more keys\"    schema-valid=\"true\"");

    assertTrue(dataEntries.containsKey("mutator"), "Expect mutator as key");
    assertTrue(dataEntries.containsKey("schema-valid"), "Expect schema-valid as key");
  }

  @Test
  @DisplayName("Allow case insensitive keys")
  void caseInsensitiveKeys() {
    dataEntries = p.parsePiData("MUTATOR=\"more keys\"");
    assertTrue(dataEntries.containsKey("mutator"), "Expect mutator as key");
  }

  @Test
  @DisplayName("Allow unknown key/value pair")
  void unknownKeyValuePair() {
    dataEntries = p.parsePiData("unknownkey=\"unknown value\"");
    assertTrue(dataEntries.containsKey("unknownkey"));
    assertTrue(dataEntries.containsValue("unknown value"));
  }

  @Test
  @DisplayName("Allow value less keys")
  void allowValueLessKeys() {
    dataEntries = p
        .parsePiData(
            "mutator=\"value  less keys \"    schema-valid schematron-valid     schematron-invalid");

    assertTrue(dataEntries.containsKey("mutator"), "Expect mutator as key");
    assertTrue(dataEntries.containsKey("schema-valid"), "Expect schema-valid as key");
    assertTrue(dataEntries.containsKey("schematron-valid"), "Expect schematron-valid as key only");
    assertTrue(dataEntries.containsKey("schematron-invalid"),
        "Expect schematron-invalid as key only");
  }

  @Test
  @DisplayName("No equal sign between keys")
  void noEqualSignBetweenKeys() {
    dataEntries = p
        .parsePiData(
            "key=between-key=\"value\" schema-valid schematron-valid     schematron-invalid");
    assertTrue(p.hasError());
  }

  @Test
  @DisplayName("value without quotes")
  void mutatorNoQuotes() {
    dataEntries = p.parsePiData("mutator=noquote");
    assertNotNull(p);
    assertTrue(p.hasError());
  }

  @Test
  @DisplayName("Do warn on duplicate Property keys")
  void detectDuplicateKeys() {
    dataEntries = p
        .parsePiData("mutator=\"remove\" key=\"first key\" key=\"second key\"");
    assertNotNull(p);
    assertTrue(p.hasError());
    assertTrue(p.getParsingStatus().matches(OperationStatus.WARNING));
  }

  @Test
  @Tag("parser")
  @DisplayName("Test schematron-valid=\"RP-1\"")
  void schematronValidOneRule() {
    final String SCH_RULE = this.mutatorEntry + " schematron-valid=\"RP-1\"";
    dataEntries = p.parsePiData(SCH_RULE);
    assertNotNull(dataEntries);
    assertTrue(dataEntries.containsKey(PropertyKeys.SCHEMATRON_VALID.toString()));
    assertEquals(dataEntries.get(PropertyKeys.SCHEMATRON_VALID.toString()), "RP-1");
  }

}
