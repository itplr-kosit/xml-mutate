package de.kosit.xmlmutate.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.kosit.xmlmutate.runner.DomFragment;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

class XMuteInstructionTest {

  @Test
  @DisplayName("Should get target fragment with correct element and text content")
  void testGetTargetFragment() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.newDocument();
    document.appendChild(document.createElement("root"));

    ProcessingInstruction pi = document.createProcessingInstruction("xmute", "mutator=remove");
    document.getDocumentElement().appendChild(pi);

    Element idElement = document.createElement("cbc:ID");
    idElement.setTextContent("vgv");
    document.getDocumentElement().appendChild(idElement);

    Properties props = new Properties();
    props.setProperty("mutator", "remove");

    XMuteInstruction instruction = new XMuteInstruction(pi, props);
    DomFragment fragment = instruction.getTargetFragment();

    Node contentNode = fragment.getContentNode();
    Node nodeElement = contentNode.getFirstChild();

    assertEquals("cbc:ID", nodeElement.getNodeName());
    assertEquals("vgv", nodeElement.getTextContent());
  }

}
