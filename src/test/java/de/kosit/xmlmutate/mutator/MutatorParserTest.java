package de.kosit.xmlmutate.mutator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;

/**
 * MutatorParserTest
 * @author Renzo Kottmann
 */
public class MutatorParserTest {

    private final static Logger log = LogManager.getLogger(MutatorParserTest.class);
    private static Document doc = null;

    @BeforeAll
    static void createEmptyDomDocument() {

        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Can not create DOM Document builder reason=" + e);
        }

        doc = builder.newDocument();

    }

    private ProcessingInstruction createXMutePI(String piData) {
        return doc.createProcessingInstruction(MutatorParser.PI_TARGET_NAME, piData);
    }

    @Test
    @DisplayName("Reject parsing of non xmute PI")
    void rejectNonXMutePI() {
        ProcessingInstruction pi = doc.createProcessingInstruction(MutatorParser.PI_TARGET_NAME + "wrong", "");
        assertThrows(MutatorException.class, () -> {
            MutatorParser.parse(pi);
        });
    }

    @Test
    @DisplayName("Reject empty PI data: minimum mutator name is expected.")
    
    void rejectEmptyPiData() {
        assertThrows(MutatorException.class, () -> {
            MutatorParser.parse( this.createXMutePI(""));
        });
    }

    @Test
    @DisplayName("Reject empty PI data: minimum mutator name is expected.")

    void rejectNullPiData() {
        assertThrows(MutatorException.class, () -> {
            MutatorParser.parse(this.createXMutePI(null));
        });
    }

    @Test
    @DisplayName("Reject XMute instruction without \"mutator\" entry")
    void rejectNoMutator() {
        assertThrows(IllegalArgumentException.class, () -> {
            MutatorParser.parse(this.createXMutePI("schema-valid=\"true\""));
        });
    }


    @Test
    @DisplayName("Reject to parse XMute instruction with an unknwn mutator name")
    
    void rejectUnknownMutator() {
        assertThrows(MutatorException.class, () -> {
            MutatorParser.parse(this.createXMutePI("mutator=\"not exisiting mutator\""));
        });
    }

    @Test
    @DisplayName("Get an empty Mutator from minimal PI")
    void parseMinimalEmptyMutator() {
        Mutator mutator = MutatorParser.parse(this.createXMutePI("mutator=\"eMpty\""));
        assertNotNull(mutator);
        log.debug(mutator.getName() + " should be equals empty");
        assertEquals(mutator.getName(), "empty");
    }

}