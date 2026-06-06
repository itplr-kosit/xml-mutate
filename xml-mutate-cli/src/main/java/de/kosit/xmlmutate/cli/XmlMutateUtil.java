package de.kosit.xmlmutate.cli;

import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml4j.core.XSLTHelper;

/**
 * XmlMutateUtil
 */

public class XmlMutateUtil {
    private static final Logger log = LoggerFactory.getLogger(XmlMutateUtil.class);

    public static Element elementFrom(String xml) throws SAXException, IOException {
        return documentFrom(new InputSource(new StringReader(xml))).getDocumentElement();
    }

    public static Document documentFrom(InputSource is)
            throws SAXException, IOException {
        DocumentBuilder builder = createDocumentBuilder();
        Document doc = builder.parse(is);
        return doc;
    }

    private static DocumentBuilder createDocumentBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Could not configure DOM DocumentBuilder.", e);
        }
        return builder;
    }

    public static String printToString(Node node) {
        return printToString(node, 2);
    }

    public static String printToString(Node node, int indent) {

        final Transformer transformer;
        String result = "";
        try (StringWriter sw = new StringWriter()) {
            StreamResult xmlResult = new StreamResult(sw);
            transformer = XSLTHelper.createTransformer(indent);
            transformer.transform(new DOMSource(node), xmlResult);
            result = xmlResult.getWriter().toString();
        } catch (TransformerException | IOException e) {
            log.error("Could not pretty print xml of node={}", node);
            throw new MutationException(ErrorCode.TRANSFORM_ERROR);
        }

        return result;
    }
}
