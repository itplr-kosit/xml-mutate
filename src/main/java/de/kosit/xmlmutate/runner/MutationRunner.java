package de.kosit.xmlmutate.runner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.validation.Validator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

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
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.kosit.xmlmutate.XMLMutateApp;
import de.kosit.xmlmutate.XMLMutateConfiguration;
import de.kosit.xmlmutate.mutator.Mutator;
import de.kosit.xmlmutate.mutator.MutatorException;
import de.kosit.xmlmutate.mutator.MutatorParser;

/**
 * MutationRunner takes as input a list of files/paths and parses each input for
 * mutation instructions and executes them
 *
 * @author Renzo Kottmann
 */
public class MutationRunner {
    private final static Logger log = LogManager.getLogger(MutationRunner.class);

    private List<Path> inputPathList = new ArrayList<Path>();
    private Path outputDir = null;
    private DocumentBuilder docBuilder;
    private Map<String, Templates> xsltCache = null;
    XMLMutateConfiguration config = null;

    private MutationRunner() {
    };

    public MutationRunner(List<Path> inputPathList, XMLMutateConfiguration config, Map<String, Templates> xsltCache) {
        this.inputPathList = inputPathList;
        this.setOutputDir(config.getOutputDir());
        this.xsltCache = xsltCache;
        this.config = config;

    }

    private void setOutputDir(Path outputDir) {
        if (outputDir == null) {
            log.error("outputdir is null");
            throw new IllegalArgumentException("Need a valid output dir instead of a null value");

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

    public int execute(final boolean testMutation) {
        log.debug("Executing Mutation runner");

        try {
            this.prepareDomFactory();
        } catch (ParserConfigurationException e) {
            log.error("Could not configure dom parseer configuration", e);
            throw new MutatorException("Could not configure dom parseer configuration", e);
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
            this.mutate(docOrigin, name, testMutation);

            // xPathAllPi =
            // XPathFactory.newInstance().newXPath().compile("//processing-instruction('xmute')");
            // xPathNextElement =
            // XPathFactory.newInstance().newXPath().compile("./following-sibling::*[position()
            // = 1 ]");
            // nodes = (NodeList) xPathAllPi.evaluate(docOrigin.getDocumentElement(),
            // XPathConstants.NODESET);
            // context = (Element) xPathNextElement.evaluate(pi, XPathConstants.NODE);
        }
        return 0;
    }

    private void mutate(Document origin, String documentName, boolean testMutation) {
        TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin,
                NodeFilter.SHOW_PROCESSING_INSTRUCTION, null, true);
        TreeWalker elemWalker = ((DocumentTraversal) origin).createTreeWalker(origin, NodeFilter.SHOW_ELEMENT, null,
                true);
        // finde pi
        // finde next elem
        // deep copy of oroginal elem as docfrag or any other way that decopuls it from
        // doc
        // gib elem an mutator (with return elem and/or anyway side effect through
        // object referece)
        // write out doc
        // replace mutated elem mit original
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
            mutator = MutatorParser.parse(pi, xsltCache);
            // copy elem to docfrag
            origFragment = origin.createDocumentFragment();
            origFragment.appendChild(mutationTargetElem.cloneNode(true));
            // mutating
            Node changed = mutator.execute(mutationTargetElem);

            this.write(origin, new NamingStrategyImpl().byId(documentName, String.valueOf(++docNum)));
            boolean schemaValid = false;
            if (testMutation) {
                log.debug("Check mutated against schema");
                schemaValid = this.testSchema(origin);
            }
            if (schemaValid == mutator.getConfig().expectSchemaValid()) {
                log.debug("Mutation resulted expected outcome :) result={} and expected={}", schemaValid,
                        mutator.getConfig().expectSchemaValid());
            } else {
                log.debug("Mutation resulted in UN-expected outcome :( , result={} and expected={}", schemaValid,
                        mutator.getConfig().expectSchemaValid());
            }

            log.debug("Replacing mutated=" + mutationTargetElem + " with original=" + origFragment.getFirstChild()
                    + " again. Parent of original=" + parent);

            parent.replaceChild(origFragment, changed);

        }
    }

    private boolean testSchema(Document doc) {
        log.debug("Validate doc=" + doc.getNodeName());
        Validator val = config.getSchema("ubl").newValidator();
        List<String> valErrors = new ArrayList<String>();
        val.setErrorHandler(new ErrorHandler() {
            @Override
            public void error(SAXParseException err) throws SAXException {
                valErrors.add(err.getMessage());
            }

            @Override
            public void fatalError(SAXParseException err) throws SAXException {
                valErrors.add(err.getMessage());
            }

            @Override
            public void warning(SAXParseException err) throws SAXException {
                valErrors.add(err.getMessage());
            }
        });
        try {
            val.validate(new DOMSource(doc));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        if (valErrors.isEmpty()) {
            log.debug("No validation erros");
            return true;
        }
        for (String var : valErrors) {
            log.debug("valErrors=" + var);
        }
        return false;
    }

    /**
     * Copy an XML document, adding it as a child of the target document root
     *
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