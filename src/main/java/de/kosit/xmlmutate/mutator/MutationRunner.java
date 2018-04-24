package de.kosit.xmlmutate.mutator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

import de.kosit.xmlmutate.XMLMutateApp;

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

    public MutationRunner(List<Path> inputPathList, Path outputDir) {
        this.inputPathList = inputPathList;
        this.setOutputDir(outputDir);
    }

    private void setOutputDir(Path outputDir) {
        if (outputDir == null) {
            log.error("outputdir is null");
            throw new IllegalArgumentException("Need a valid outpt dir instead of a null value");

        }
        if (!Files.isDirectory(outputDir)) {
            throw new IllegalArgumentException("Output path must be a valid directory");
        }
        if (!Files.isWritable(outputDir)) {
            throw new IllegalArgumentException("Output directory must be writable by user");
        }
        this.outputDir = outputDir;
    }

    private void prepareDomFactory() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(true);
        docFactory.setNamespaceAware(true);
        docFactory.setIgnoringElementContentWhitespace(true);
        docBuilder = docFactory.newDocumentBuilder();

    }

    private void write(Document doc, NamingStrategy name) {
        log.debug("Writing to dir=" + outputDir);
        log.debug("Writing to file name=" + name.getFileName());
        Path out = Paths.get(outputDir.toString(), name.getFileName());

        try {
            XMLMutateApp.printDocument(doc, new FileOutputStream(out.toFile()));
        } catch (IOException | TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

        Document docOrigin = null;

        for (Path file : inputPathList) {
            Files.isReadable(file);

            try {
                docOrigin = docBuilder.parse(Files.newInputStream(file));

            } catch (SAXException | IOException e) {
                // TODO Auto-generated catch block
                log.error("Could not Parse XML Instance", e);
                e.printStackTrace();
                return 4;
            }
            docOrigin.normalize();
            docOrigin.normalizeDocument();

            String name = file.getFileName().toString();
            name = name.replaceFirst("\\.xml", "");
            log.debug("Doc name=" + name);
            log.debug("Doc URI=" + docOrigin.getDocumentURI());
            this.mutate(docOrigin, name);

            // xPathAllPi = XPathFactory.newInstance().newXPath().compile("//processing-instruction('xmute')");
            // xPathNextElement = XPathFactory.newInstance().newXPath().compile("./following-sibling::*[position() = 1 ]");
            // nodes = (NodeList) xPathAllPi.evaluate(docOrigin.getDocumentElement(), XPathConstants.NODESET);
            // context = (Element) xPathNextElement.evaluate(pi, XPathConstants.NODE);
        }
        return 0;
    }

    private void mutate(Document origin, String documentName) {
        TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin,
                NodeFilter.SHOW_PROCESSING_INSTRUCTION, null, true);
        TreeWalker elemWalker = ((DocumentTraversal) origin).createTreeWalker(origin, NodeFilter.SHOW_ELEMENT, null,
                true);
        // finde pi
        // finde next elem
        // deep copy of elem as docfrag or any other way that decopuls it from doc
        // gib elem an mutator (with return elem and/or anyway side effect through object referece)
        // write out doc
        // replace mutated elem mit vorheriger kopie
        Element mutationTargetElem = null;
        DocumentFragment origFragment = null;
        Mutator mutator = null;
        ProcessingInstruction pi = null;
        int docNum = 0;
        while (piWalker.nextNode() != null) {
            pi = (ProcessingInstruction) piWalker.getCurrentNode();
            log.debug("PI=" + pi);

            elemWalker.setCurrentNode(piWalker.getCurrentNode());

            mutationTargetElem = (Element) elemWalker.nextNode();
            // because walker also moves to parent, call after walker.nextNode() above
            Element parent = (Element) elemWalker.parentNode();

            // now we know we have a valid pi and an elem
            mutator = MutatorParser.parse(pi);
            // copy elem to docfrag
            origFragment = origin.createDocumentFragment();
            origFragment.appendChild(mutationTargetElem.cloneNode(true));
            // mutating
            Node changed = mutator.execute(mutationTargetElem);
            this.write(origin, new NamingStrategyImpl().byId(documentName, String.valueOf(++docNum)));

            log.debug("Replacing mutated=" + mutationTargetElem + " with original=" + origFragment.getFirstChild()
                    + " again. Parent of original=" + parent);

            parent.replaceChild(origFragment, changed);

        }
    }

    /**
    * Copy an XML document, adding it as a child of the target document root
    * @param source Document to copy
    * @param target Document to contain copy
    */
    private Document copyDocument(Document source) {
        Document target = docBuilder.newDocument();
        Node node = target.importNode(source.getDocumentElement(), true);

        target.getDocumentElement().appendChild(node);
        return target;
    }
}