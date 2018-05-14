package de.kosit.xmlmutate.tester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.XMLMutateApp;

/**
 * SchematronTester
 *
 * @author Renzo Kottmann
 */
public class SchematronTester {

    private final static Logger log = LogManager.getLogger(SchematronTester.class);
    Transformer xsltTransformer = null;
    String schematronName = "";

    private SchematronTester() {
    }

    public SchematronTester(String schematronName, String schematronFile) {
        this.schematronName = schematronName;
        this.initTransformer(schematronFile);
    }

    private void initTransformer(String schematronFile) {

        Templates schematron = null;
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // transformerFactory.
        StreamSource xsltSource = new StreamSource(new File(schematronFile));

        try {
            schematron = transformerFactory.newTemplates(xsltSource);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            log.error("Error loadding xslt", e);
        }
        try {
            xsltTransformer = schematron.newTransformer();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            log.error("Could not configure schematron tester =" + schematron, e);
        }
    }

    public List<TestReport> test(Document doc, List<Expectation> expectation) {

        log.debug("Validate doc=" + doc.getNodeName());
        DOMResult result = new DOMResult();
        DOMSource xmlSource = new DOMSource(doc);
        try {
            xsltTransformer.transform(xmlSource, result);
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            log.error("Could not schematron check" + doc, e);
        }
        Node resultNode = result.getNode();
        Node resultChild = resultNode.getFirstChild();
        log.debug("Got result node=" + resultNode.getNodeName() + " of type=" + resultNode.getNodeType());
        log.debug("Got resultChild node=" + resultChild.getNodeName() + " of type=" + resultChild.getNodeType());
        if (resultChild.getNodeType() == Node.ELEMENT_NODE) {
            log.debug("Is element node!");
        }
        try {
            log.debug("Print schmematron result");
            XMLMutateApp.printDocument((Document) resultNode, System.out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<TestReport> reports = new ArrayList<TestReport>();
        List<String> failedAsserts = parseFailedAsserts(resultNode);

        // having no expectations means all should be valid, therefore having empty
        // expection list but failed assert means all falied did not meet expectation
        if (expectation.isEmpty()) {
            for (String failed : failedAsserts) {
                reports.add(new TestReport(failed, true, false));
            }
        }
        // if expectation.isEmpty() the following loop does not do any iteration
        for (Expectation expec : expectation) {
            String tested = expec.of();
            if (failedAsserts.contains(tested)) {
                reports.add(new TestReport(tested, expec.is(), false));
            } else {
                reports.add(new TestReport(tested, expec.is(), true));
            }
        }

        return reports;
    }

    private List<String> parseFailedAsserts(Node resultNode) {
        List<String> failedAsserts = new ArrayList<String>();
        // xPathAllPi =
        // XPathFactory.newInstance().newXPath().compile("//processing-instruction('xmute')");
        // xPathNextElement =
        // XPathFactory.newInstance().newXPath().compile("./following-sibling::*[position()
        // = 1 ]");
        // nodes = (NodeList) xPathAllPi.evaluate(docOrigin.getDocumentElement(),
        // XPathConstants.NODESET);
        // context = (Element) xPathNextElement.evaluate(pi, XPathConstants.NODE);
        return failedAsserts;
    }

    public class TestReport {
        String what = "";
        // chosing different boolean values such that isAsExcpectd does not result in
        // true by default
        boolean expected = false;
        boolean actual = true;

        private TestReport() {
        }

        public TestReport(String what, boolean expected, boolean actual) {
            this.what = what;
            this.expected = expected;
            this.actual = actual;
        }

        public String of() {
            return what;
        }

        public boolean expected() {
            return expected;
        }

        public boolean actual() {
            return actual;
        }

        public boolean asExpected() {
            return expected == actual;
        }
    }

    private class Expectation {

        String what = "";
        boolean valid = false;

        Expectation(String what, boolean valid) {
            this.what = what;
            this.valid = valid;

        }

        public String of() {
            return what;
        }

        public boolean is() {
            return valid;
        }

    }

}