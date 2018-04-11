package de.kosit.xmlmutate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import de.kosit.xmlmutate.mutator.MutationRunner;



/**
 * Hello world!
 *
 */
public class XMLMutateApp {

    private final static Logger log = LogManager.getLogger(XMLMutateApp.class);

    private XMLMutateConfiguration config = null;
    private List<Path> inputPathList = new ArrayList<Path>();

    public XMLMutateApp() {
    }

    public XMLMutateApp(String[] line) {
        this();
        XMLMutateConfigurator mc = new XMLMutateConfigurator();
        this.config = mc.fromCommandLine(line);
        this.inputPathList = mc.getInputPaths();
        mc = null;
    }

    public int run() {
        int exitCode = 1;
        switch (config.getRunMode()) {
        case "mutate":
            exitCode = this.runMutate();
            break;

        default:
            break;
        }
        return exitCode;
    }

    public int runMutate() {
        log.debug("Run in mutate only mode");
        MutationRunner runner =  new MutationRunner(this.inputPathList);
        return runner.execute();
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
