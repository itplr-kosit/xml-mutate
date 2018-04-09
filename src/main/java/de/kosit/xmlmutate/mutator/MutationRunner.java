package de.kosit.xmlmutate.mutator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

/**
 * MutationRunner takes as input a list of files/paths and 
 * parses each input for mutation instructions and 
 * executes them
 * @author Renzo Kottmann
 */
public class MutationRunner {
    private final static Logger log = LogManager.getLogger(MutationRunner.class);

    private List<Path> inputPathList = new ArrayList<Path>();
    private Path outputDir = null;
    private DocumentBuilder docBuilder;

    public MutationRunner(List<Path> inputPathList) {
        this.inputPathList = inputPathList;
    }

    private void prepareDomFactory() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(true);
        docFactory.setNamespaceAware(true);
        docFactory.setIgnoringElementContentWhitespace(true);
        docBuilder = docFactory.newDocumentBuilder();

    }

    public int execute() {
        log.debug("Executing Mutation runner");
        //TODO change rethrow an own Mutationapp runtime exception
        try {
            this.prepareDomFactory();
        } catch (ParserConfigurationException e) {
            log.error("Could not configure dom parseer configuration", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 4;
        }

        Document doc = null;

        for (Path file : inputPathList) {
            Files.isReadable(file);

            try {
                doc = docBuilder.parse(Files.newInputStream(file));
            } catch (SAXException | IOException e) {
                // TODO Auto-generated catch block
                log.error("Could not Parse XML Instance", e);
                e.printStackTrace();
                return 4;
            }
            doc.normalize();
            doc.normalizeDocument();

            XPathExpression xPathAllPi = null;
            XPathExpression xPathNextElement = null;
            try {
                xPathAllPi = XPathFactory.newInstance().newXPath().compile("//processing-instruction('xmute')");
                xPathNextElement = XPathFactory.newInstance().newXPath()
                        .compile("./following-sibling::*[position() = 1 ]");

            NodeList nodes = null;
            
                nodes = (NodeList) xPathAllPi.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

                ProcessingInstruction pi = null;
                Node context = null;
                Mutator mutator = null;
                for (int i = 0; i < nodes.getLength(); i++) {
                    pi = (ProcessingInstruction) nodes.item(i);
                    // mutator = MutatorParser.parse(pi);
                    context = (Node) xPathNextElement.evaluate(pi, XPathConstants.NODE);
                    log.debug(pi);
                    
                    if (context.getNodeType() == Node.ELEMENT_NODE) {
                        log.debug("Element to remove" + context);
                    }

                    
                }
            } catch (XPathExpressionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return 10;
            }

        }
        return 0;
    }
}