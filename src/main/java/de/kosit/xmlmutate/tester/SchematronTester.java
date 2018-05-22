package de.kosit.xmlmutate.tester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kosit.xmlmutate.XMLMutateApp;
import de.kosit.xmlmutate.mutator.MutatorException;

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
        if (Objects.isNull(schematronFile)) {
            throw new MutatorException("File name can not be null");
        }
        Templates schematron = null;
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        StreamSource xsltSource = new StreamSource(new File(schematronFile));

        try {
            schematron = transformerFactory.newTemplates(xsltSource);
        } catch (TransformerConfigurationException e) {
            throw new MutatorException("Error loadding xslt", e);
        }
        try {
            xsltTransformer = schematron.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new MutatorException(String.format("Could not create schematron name=%s with xslt=%s",
                    this.schematronName, schematronFile), e);
        }
    }

    public List<TestItem> test(Document doc, List<Expectation> expectation) {
        if (Objects.isNull(doc)) {
            throw new IllegalArgumentException("Document should not be null");
        }
        if (Objects.isNull(expectation)) {
            log.error("Expectations should not be null. Creating empty list for now..");
            expectation = new ArrayList<Expectation>();
        }

        log.debug("Validate doc=" + doc.getNodeName());

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Document svrl = documentBuilder.newDocument();
        DOMResult result = new DOMResult(svrl);
        DOMSource xmlSource = new DOMSource(doc);
        try {
            this.xsltTransformer.transform(xmlSource, result);
        } catch (TransformerException e) {
            throw new MutatorException(
                    String.format("Could not check doc=%s against schematron=%s", doc, this.schematronName), e);
        }
        Node resultNode = result.getNode();
        Node resultChild = resultNode.getFirstChild();
        log.debug("Got result node=" + resultNode.getNodeName() + " of type=" + resultNode.getNodeType());
        log.debug("Got resultChild node=" + resultChild.getNodeName() + " of type=" + resultChild.getNodeType());

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

        List<TestItem> reports = new ArrayList<TestItem>();

        Map<String, SchematronTestItemDetail> failedAsserts = parseFailedAsserts(result.getNode());

        // having no expectations means all should be valid, therefore having empty
        // expectation list but failed assert means all falied did not meet expectation
        if (expectation.isEmpty()) {
            log.debug("got no expectations");
            for (SchematronTestItemDetail failed : failedAsserts.values()) {
                reports.add(new SchematronTestItem(failed.getId(), true, false, failed));
            }
        }
        // if expectation.isEmpty() the following loop does not do any iteration
        for (Expectation expec : expectation) {
            String tested = expec.what().toLowerCase();
            if (failedAsserts.containsKey(tested)) {
                reports.add(new SchematronTestItem(tested, expec.is(), false, failedAsserts.get(tested)));
            } else {
                reports.add(new SchematronTestItem(tested, expec.is(), true, failedAsserts.get(tested)));
            }
        }

        return reports;
    }

    private Map<String, SchematronTestItemDetail> parseFailedAsserts(Node resultNode) {
        log.debug("parse failed asserts");
        Map<String, SchematronTestItemDetail> failedAsserts = new HashMap<String, SchematronTestItemDetail>();
        XPath xp = null;
        XPathExpression allFailedAsserts;

        try {
            xp = XPathFactory.newInstance().newXPath();
            xp.setNamespaceContext(new NamespaceContext() {

                @Override
                public Iterator getPrefixes(String prefix) {
                    return null;
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return null;
                }

                @Override
                public String getNamespaceURI(String prefix) {
                    if (prefix.equals("svrl")) {
                        return "http://purl.oclc.org/dsdl/svrl";
                    } else {
                        return null;
                    }
                }
            });
            allFailedAsserts = xp.compile("//svrl:failed-assert");
            NodeList nodes = (NodeList) allFailedAsserts.evaluate(resultNode, XPathConstants.NODESET);
            log.debug("Num failed asserts=" + nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                NamedNodeMap attMap = node.getAttributes();
                // mandatory svrl attribute
                Node att = attMap.getNamedItem("location");
                String location = att.getNodeValue();
                // mandatory svrl attribute
                att = attMap.getNamedItem("test");
                String test = att.getNodeValue();

                att = attMap.getNamedItem("id");
                String id = (Objects.isNull(att)) ? location : att.getNodeValue();

                att = attMap.getNamedItem("role");
                String role = (Objects.isNull(att)) ? "" : att.getNodeValue();

                att = attMap.getNamedItem("flag");
                String flag = (Objects.isNull(att)) ? "" : att.getNodeValue();

                failedAsserts.put(id.toLowerCase(), new SchematronTestItemDetail(id, location, test, role, flag));
            }
        } catch (XPathExpressionException e) {
            throw new MutatorException("Could not parse svrl failed asserts", e);
        }

        return failedAsserts;
    }

    

}