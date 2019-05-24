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
import de.kosit.xmlmutate.mutation.Expectation;

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

    public SchematronTester(final String schematronName, final String schematronFile) {
        this.schematronName = schematronName;
        this.initTransformer(schematronFile);
    }

    private void initTransformer(final String schematronFile) {
        if (Objects.isNull(schematronFile)) {
            // throw new MutatorException("File name can not be null", e);
        }
        Templates schematron = null;
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();

        final StreamSource xsltSource = new StreamSource(new File(schematronFile));

        try {
            schematron = transformerFactory.newTemplates(xsltSource);
        } catch (final TransformerConfigurationException e) {
            // throw new MutatorException("Error loadding xslt", e);
        }
        try {
            this.xsltTransformer = schematron.newTransformer();
        } catch (final TransformerConfigurationException e) {
            // throw new MutatorException(String.format("Could not create schematron name=%s with xslt=%s",
            // this.schematronName, schematronFile), e);
        }
    }

    public List<TestItem> test(final Document doc, List<Expectation> expectation) {
        if (Objects.isNull(doc)) {
            throw new IllegalArgumentException("Document should not be null");
        }
        if (Objects.isNull(expectation)) {
            log.error("Expectations should not be null. Creating empty list for now..");
            expectation = new ArrayList<Expectation>();
        }

        log.debug("Validate doc=" + doc.getNodeName());

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        final Document svrl = documentBuilder.newDocument();
        final DOMResult result = new DOMResult(svrl);
        final DOMSource xmlSource = new DOMSource(doc);
        try {
            this.xsltTransformer.transform(xmlSource, result);
        } catch (final TransformerException e) {
            // throw new MutatorException(
            // String.format("Could not check doc=%s against schematron=%s", doc, this.schematronName), e);
        }
        final Node resultNode = result.getNode();
        final Node resultChild = resultNode.getFirstChild();
        log.debug("Got result node=" + resultNode.getNodeName() + " of type=" + resultNode.getNodeType());
        log.debug("Got resultChild node=" + resultChild.getNodeName() + " of type=" + resultChild.getNodeType());

        try {
            log.debug("Print schmematron result");
            XMLMutateApp.printDocument((Document) resultNode, System.out);
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final List<TestItem> reports = new ArrayList<TestItem>();

        final Map<String, SchematronTestItemDetail> failedAsserts = parseFailedAsserts(result.getNode());

        // having no expectations means all should be valid, therefore having empty
        // expectation list but failed assert means all falied did not meet expectation
        if (expectation.isEmpty()) {
            log.debug("got no expectations");
            for (final SchematronTestItemDetail failed : failedAsserts.values()) {
                reports.add(new SchematronTestItem(failed.getId(), true, false, failed));
            }
        }
        // if expectation.isEmpty() the following loop does not do any iteration
        for (final Expectation expec : expectation) {
            final String tested = expec.ruleName().toLowerCase();
            if (failedAsserts.containsKey(tested)) {
                reports.add(new SchematronTestItem(tested, expec.mustFail(), false, failedAsserts.get(tested)));
            } else {
                reports.add(new SchematronTestItem(tested, expec.mustFail(), true, failedAsserts.get(tested)));
            }
        }

        return reports;
    }

    private static Map<String, SchematronTestItemDetail> parseFailedAsserts(final Node resultNode) {
        log.debug("parse failed asserts");
        final Map<String, SchematronTestItemDetail> failedAsserts = new HashMap<String, SchematronTestItemDetail>();
        XPath xp = null;
        final XPathExpression allFailedAsserts;

        try {
            xp = XPathFactory.newInstance().newXPath();
            xp.setNamespaceContext(new NamespaceContext() {

                @Override
                public Iterator getPrefixes(final String prefix) {
                    return null;
                }

                @Override
                public String getPrefix(final String namespaceURI) {
                    return null;
                }

                @Override
                public String getNamespaceURI(final String prefix) {
                    if (prefix.equals("svrl")) {
                        return "http://purl.oclc.org/dsdl/svrl";
                    } else {
                        return null;
                    }
                }
            });
            allFailedAsserts = xp.compile("//svrl:failed-assert");
            final NodeList nodes = (NodeList) allFailedAsserts.evaluate(resultNode, XPathConstants.NODESET);
            log.debug("Num failed asserts=" + nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                final Node node = nodes.item(i);
                final NamedNodeMap attMap = node.getAttributes();
                // mandatory svrl attribute
                Node att = attMap.getNamedItem("location");
                final String location = att.getNodeValue();
                // mandatory svrl attribute
                att = attMap.getNamedItem("test");
                final String test = att.getNodeValue();

                att = attMap.getNamedItem("id");
                final String id = (Objects.isNull(att)) ? location : att.getNodeValue();

                att = attMap.getNamedItem("role");
                final String role = (Objects.isNull(att)) ? "" : att.getNodeValue();

                att = attMap.getNamedItem("flag");
                final String flag = (Objects.isNull(att)) ? "" : att.getNodeValue();

                failedAsserts.put(id.toLowerCase(), new SchematronTestItemDetail(id, location, test, role, flag));
            }
        } catch (final XPathExpressionException e) {
            // throw new MutatorException("Could not parse svrl failed asserts", e);
        }

        return failedAsserts;
    }

    

}