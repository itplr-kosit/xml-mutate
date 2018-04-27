package de.kosit.xmlmutate.mutator;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.XMLMutateApp;

/**
 * AddElementMutator
 * @author Renzo Kottmann
 */
public class AddElementMutator implements Mutator {

    private final static Logger log = LogManager.getLogger(AddElementMutator.class);

    private final static String MUTATOR_NAME = "add";

    private MutatorConfig config = null;
    private Templates xslt = null;

    public AddElementMutator(MutatorConfig config, Templates xslt) {
        this.addConfig(config);
        this.xslt = xslt;
    }

    @Override
    public void addConfig(MutatorConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return MUTATOR_NAME.toLowerCase();
    }

    @Override
    public Node execute(Element context) {

        Transformer xsltTransformer = null;

        try {
            xsltTransformer = xslt.newTransformer();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            log.error("Could not configure transformer=" + xslt, e);
        }
        Document origin = context.getOwnerDocument();
        // DocumentFragment resultFragment = null;
        // resultFragment = context.getOwnerDocument().createDocumentFragment();

        xsltTransformer.setParameter("param1", "value1");
        xsltTransformer.setParameter("param2", "value2");
        DOMResult result = new DOMResult();
        Source xmlSource = new DOMSource(context);
        try {
            xsltTransformer.transform(xmlSource, result);
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            log.error("Could not transform element=" + context, e);
        }
        Node resultNode = result.getNode();
        Node resultChild = resultNode.getFirstChild();
        log.debug("Got result node=" + resultNode.getNodeName() + " of type=" + resultNode.getNodeType());
        log.debug("Got resultChild node=" + resultChild.getNodeName() + " of type=" + resultChild.getNodeType());
        if (resultChild.getNodeType() == Node.ELEMENT_NODE) {
            log.debug("Is element node!");
        }
        try {
            log.debug("Print result");
            XMLMutateApp.printDocument((Document) resultNode, System.out);
        } catch (IOException | TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Node adopted = origin.adoptNode(resultChild);
        //resultFragment.appendresultChild(result.getNode().cloneNode(true));
        Node parent = context.getParentNode();
        parent.replaceChild(adopted, context);
        return adopted;
    }

}