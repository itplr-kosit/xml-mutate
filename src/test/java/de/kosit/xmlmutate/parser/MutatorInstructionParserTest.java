package de.kosit.xmlmutate.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.kosit.xmlmutate.mutation.XMuteInstruction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;

class MutatorInstructionParserTest {

  private final MutatorInstructionParser parser = new MutatorInstructionParser();

  @Test
  @DisplayName("Should return XMuteInstruction with correct properties")
  void testParseInstruction_ReturnsXMuteInstruction() throws ParserConfigurationException {
    ProcessingInstruction pi = getProcessingInstriction();

    pi.setTextContent("mutator=\"code\" values=\"not-valid-code, kbeh\" schematron-invalid=\"efde:CL-DE-BT-11\"");

    XMuteInstruction instruction = parser.parseInstruction(pi);

    assertNotNull(instruction);
    assertEquals("code", instruction.getProperty("mutator"));
    assertEquals("not-valid-code, kbeh", instruction.getProperty("values"));
    assertEquals("efde:CL-DE-BT-11", instruction.getProperty("schematron-invalid"));
  }

  @Test
  @DisplayName("Should return XMuteInstruction with error for empty content")
  void testParseInstruction_ReturnsXMuteInstructionWithError()
      throws ParserConfigurationException {
    ProcessingInstruction pi = getProcessingInstriction();
    pi.setTextContent("");

    XMuteInstruction instruction = parser.parseInstruction(pi);

    assertNotNull(instruction);
    assertTrue(instruction.hasError());
  }

  ProcessingInstruction getProcessingInstriction() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.newDocument();
    return document.createProcessingInstruction("xmute", "mutator=\"code\" values=\"not-valid-code, kbeh\" schematron-invalid=\"efde:CL-DE-BT-11\" ");
  }

}
