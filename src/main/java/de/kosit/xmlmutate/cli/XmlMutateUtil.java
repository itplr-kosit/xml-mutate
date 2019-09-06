package de.kosit.xmlmutate.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.init.kosit.commons.Result;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * XmlMutateUtil
 */

public class XmlMutateUtil {
    private static final Logger log = LoggerFactory.getLogger(XmlMutateUtil.class);

    public static String printToString(Node node) {
        return printToString(node, 2);
    }

    public static String printToString(Node node, int indent) {

        final Transformer transformer;
        String result = "";
        try (StringWriter sw = new StringWriter()) {
            StreamResult xmlResult = new StreamResult(sw);
            transformer = createTransformer(indent, true);
            transformer.transform(new DOMSource(node), xmlResult);
            result = xmlResult.getWriter().toString();
        } catch (TransformerException | IOException e) {
            log.error("Could not pretty print xml of node={}", node);
            throw new MutationException(ErrorCode.TRANSFORM_ERROR);
        }

        return result;
    }

    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {

        Transformer transformer = createTransformer(2);

        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    public static void printDocument(Node node, OutputStream out) throws IOException, TransformerException {

        Transformer transformer = createTransformer(2);

        transformer.transform(new DOMSource(node), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    public static void printDocument(Node node, OutputStream out, int indent) throws IOException, TransformerException {

        Transformer transformer = createTransformer(indent);

        transformer.transform(new DOMSource(node), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    private static Transformer createTransformer(int indent, boolean declaration)
            throws TransformerConfigurationException {
        Transformer transformer = createTransformer(indent);

        String answer = "no";
        if (declaration) {
            answer = "yes";
        }

        // declaration ? answer = "yes" : answer = "no";
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, answer);
        return transformer;
    }

    private static Transformer createTransformer(int indent) throws TransformerConfigurationException {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        if (indent > 0) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
        }

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        return transformer;
    }

}
