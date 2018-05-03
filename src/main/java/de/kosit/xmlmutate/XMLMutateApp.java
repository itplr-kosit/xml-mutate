package de.kosit.xmlmutate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.runner.MutationRunner;

/**
 * Hello world!
 *
 */
public class XMLMutateApp {

    private final static Logger log = LogManager.getLogger(XMLMutateApp.class);

    private XMLMutateConfiguration config = null;
    private List<Path> inputPathList = new ArrayList<Path>();
    private Map<String, Templates> xsltCache = null;

    public XMLMutateApp() {
        this.config = XMLMutateConfigurator.byDefault();
    }

    public XMLMutateApp(String[] line) {
        this();
        this.config = XMLMutateConfigurator.fromCommandLine(line);
        log.debug(config);
        this.inputPathList = XMLMutateConfigurator.getInputPaths();

    }

    public XMLMutateConfiguration getConfiguration() {
        return this.config;
    }

    public int run() {
        this.xsltCache = this.loadAllTransformer();
        int exitCode = 0;
        switch (config.getRunMode()) {
        case GENERATE:
            this.runMutate(false);
            break;
        case TEST:
            this.runMutate(true);
            break;
        case CHECK:
            this.check();
            break;

        default:
            break;
        }
        return exitCode;
    }

    protected Map<String, Templates> loadAllTransformer() {
        final HashMap<String, Templates> map = new HashMap<String, Templates>();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource xsltSource = new StreamSource(getClass().getResourceAsStream("/xslt/add.xslt"));

        Templates templ = null;
        try {
            templ = transformerFactory.newTemplates(xsltSource);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            log.error("Error loadding xslt", e);
        }
        map.put("add", templ);

        return map;

    }

    public int runMutate(boolean testMutations) {

        log.debug("Run in mutate with tests=" + testMutations);
        MutationRunner runner = new MutationRunner(this.inputPathList, config, this.xsltCache);
        return runner.execute(testMutations);
    }

    public void check() {
        throw new UnsupportedOperationException("Check needs to be implemented!!");
    }

    public static void printDocument(Node node, OutputStream out)
            throws ParserConfigurationException, IOException, TransformerException {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        // Node adopted = doc.adoptNode(node);
        Node imported = doc.importNode(node, true);
        doc.appendChild(imported);
        printDocument(doc, out);
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
        out.close();
    }

    public static void main(String[] args) throws XPathExpressionException {

        log.info("Starting XML MutaTe");

        XMLMutateApp app = new XMLMutateApp(args);
        int exitCode = app.run();

        System.exit(exitCode);
    }
}
