package de.kosit.xmlmutate.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kosit.xmlmutate.cli.XmlMutateUtil;

/**
 * @author Andreas Penski
 */
public class DocumentParserTest {
    private static final Logger log = LoggerFactory.getLogger(DocumentParserTest.class);
    Document document = null;

    @Test

    void testParse() {
        this.document = DocumentParser.readDocument(Paths.get("src/test/resources/ubl-invoice-add-mutation-tests.xml"));
        assertNotNull(document);
    }

    @Test
    @Tag("current")
    void testEmptyDocFrag() {

        final String SIMPLE_XML = "<root><?xmute mutator=\"remove\" ?><e></e></root>";

        Document doc = DocumentParser.readDocument(SIMPLE_XML);
        DocumentFragment frag = doc.createDocumentFragment();
        assertNull(frag.getFirstChild());
        // frag.appendChild(
        // doc.createElement("greatcomment").cloneNode(true)
        // .appendChild(doc.createElement("herewego").cloneNode(true)));
        NodeList list = doc.getElementsByTagName("e");
        assertEquals(list.getLength(), 1);
        Node parent = doc.getFirstChild();
        assertNotNull(parent);
        log.debug("parent=" + parent.toString());
        Node node = list.item(0);
        log.trace("here=" + node.toString());
        parent.replaceChild(frag, node);

        log.debug("xml={}", XmlMutateUtil.printToString(doc, 2));

    }

}
