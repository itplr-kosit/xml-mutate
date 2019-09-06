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
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Andreas Penski
 */
public class DocumentParserTest {

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
        System.out.println("parent=" + parent.toString());
        Node node = list.item(0);
        System.out.println("here=" + node.toString());
        parent.replaceChild(frag, node);

        System.out.println("frag=" + doc);
        try {
            printDocument(doc, System.out);
        } catch (IOException | TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }
}
