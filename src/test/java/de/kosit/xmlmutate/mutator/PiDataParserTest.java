package de.kosit.xmlmutate.mutator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * MutatorParserTest
 *
 * @author Renzo Kottmann
 */
@TestInstance(Lifecycle.PER_CLASS)
public class PiDataParserTest {

    private PiDataParser p = null;

    @BeforeAll
    void init() {
        this.p = new PiDataParser();
    }

    @BeforeEach
    void newParser() {
        this.p = new PiDataParser();
    }

    @Test
    @DisplayName("Return empty map if empty pi data")
    void emptyPIData() {

        Map<String, String> dataEntries = p.parsePiData("");
        assertTrue(dataEntries.isEmpty());
    }

    @Test
    @DisplayName("Return empty map if null pi data")
    void noPIData() {

        Map<String, String> dataEntries = p.parsePiData(null);
        assertTrue(dataEntries.isEmpty());
    }

    @Test
    @DisplayName("Get result on minimum correct pi data")
    void haveResultOnMinimalCorrectInput() {
        Map<String, String> dataEntries = p.parsePiData("mutator=\"hi\"");
        assertTrue(!dataEntries.isEmpty());
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
        Map<String, String> dataEntries = p.parsePiData("mutator=\"more keys\"    schema-valid=\"true\"");

        assertTrue(dataEntries.containsKey("mutator"), "Expect mutator as key");
        assertTrue(dataEntries.containsKey("schema-valid"), "Expect schema-valid as key");
    }

    @Test
    @DisplayName("Allow value less keys")
    void allowValueLessKeys() {
        Map<String, String> dataEntries = p
                .parsePiData("mutator=\"value  less keys \"    schema-valid schematron-valid     schematron-invalid");

        assertTrue(dataEntries.containsKey("mutator"), "Expect mutator as key");
        assertTrue(dataEntries.containsKey("schema-valid"), "Expect schema-valid as key");
        assertTrue(dataEntries.containsKey("schematron-valid"), "Expect schematron-valid as key only");
        assertTrue(dataEntries.containsKey("schematron-invalid"), "Expect schematron-invalid as key only");
    }

}
