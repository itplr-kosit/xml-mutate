package de.kosit.xmlmutate.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.validation.Validator;
import javax.xml.parsers.DocumentBuilder;
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
import de.kosit.xmlmutate.XMLMutateException;
import de.kosit.xmlmutate.XMLMutateManufactory;
import de.kosit.xmlmutate.mutator.Mutator;
import de.kosit.xmlmutate.mutator.MutatorException;
import de.kosit.xmlmutate.mutator.MutatorParser;
import de.kosit.xmlmutate.tester.SchematronTester;
import de.kosit.xmlmutate.tester.TestItem;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

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
    private XMLMutateConfiguration config = null;
    private MutateReport report = null;

    private MutationRunner() {
    };

    public MutationRunner(List<Path> inputPathList, XMLMutateConfiguration config, Map<String, Templates> xsltCache) {
        this.inputPathList = inputPathList;
        this.setOutputDir(config.getOutputDir());
        this.xsltCache = xsltCache;
        this.config = config;
        this.report = new MutateReportText();
        this.report.addConfig(config);

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

    public int execute(final boolean testMutation) throws XMLMutateException {
        log.debug("Executing Mutation runner");

        Document docOrigin = null;
        int numDoc = 0;
        for (Path file : inputPathList) {
            Files.isReadable(file);
            docOrigin = XMLMutateManufactory.domDocumentFromPath(file);
            docOrigin.normalize();
            docOrigin.normalizeDocument();

            String name = file.getFileName().toString();
            name = name.replaceFirst("\\.xml", "");
            log.debug("Doc name=" + name);
            log.debug("Doc URI=" + docOrigin.getDocumentURI());
            this.mutate(docOrigin, name, testMutation);
            numDoc++;
        }
        report.setNumDoc(numDoc);
        try {
            report.write(this.outputDir.toString(), "MutaTe-Report.txt");
        } catch (IOException e) {
            throw new XMLMutateException("Could not write report to" + this.outputDir,
                    XMLMutateException.Status.FILE_ERROR);
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
        // String schematron =
        // XMLMutateManufactory.fileFromClasspath("XRechnung-UBL-validation-Invoice.xsl");
        // SchematronTester st = new SchematronTester("xr-ubl-in", schematron);

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
            NamingStrategy namingStrategy = new NamingStrategyImpl().byId(documentName, String.valueOf(++docNum));
            this.write(origin, namingStrategy);
            boolean schemaValid = false;

            if (testMutation) {
                log.debug("Check mutated against schema");
                schemaValid = this.testSchema(origin);
                Map<String, SchematronTester> schematronTester = config.getAllSchematronTester();

                for (SchematronTester st : schematronTester.values()) {
                    List<TestItem> testReport = st.test(origin, mutator.getConfig().getSchematronExpectations());
                    this.report.addDocumentReport(namingStrategy.getName(), testReport);

                }

            }
            String schemaTestStatement = "";
            if (schemaValid == mutator.getConfig().expectSchemaValid()) {
                schemaTestStatement = String.format("Mutation has expected outcome :) result=%s and expected=%s",
                        schemaValid, mutator.getConfig().expectSchemaValid());

            } else {
                schemaTestStatement = String.format(
                        "Mutation resulted in UN-expected outcome :( , result=%s and expected=%s", schemaValid,
                        mutator.getConfig().expectSchemaValid());

            }
            this.report.addSchemaTestSatement(namingStrategy.getName(), schemaTestStatement);
            log.debug(schemaTestStatement);

            log.debug("Replacing mutated=" + mutationTargetElem + " with original=" + origFragment.getFirstChild()
                    + " again. Parent of original=" + parent);

            parent.replaceChild(origFragment, changed);

        } // end mutations
        this.report.addToMutationCount(docNum);
    }

    private boolean testSchematronSaxon(Document doc) {
        DOMSource xmlSource = new DOMSource(doc);
        Processor processor = new Processor(false);
        net.sf.saxon.s9api.DocumentBuilder documentBuilder = processor.newDocumentBuilder();
        try {
            XdmNode node = documentBuilder.build(xmlSource);
            XsltCompiler xsltCompiler = processor.newXsltCompiler();
            XsltExecutable schematronValidator = xsltCompiler.compile(new StreamSource(new File(
                    "D:/git-repos/validator-configuration-xrechnung/build/resources/xrechnung/1.1/xsl/XRechnung-UBL-validation-Invoice.xsl")));
            XsltTransformer transformer = schematronValidator.load();
            transformer.setInitialContextNode(node);
            XdmDestination output = new XdmDestination();
            transformer.setDestination(output);
            // processor.

            transformer.transform();
        } catch (SaxonApiException e) {
            throw new MutatorException("saxon issue");
        }
        return false;
    }

    private boolean testSchematron(Document doc) {
        log.debug("Validate doc=" + doc.getNodeName());
        Templates schematron = null;
        Transformer xsltTransformer = null;
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // transformerFactory.
        StreamSource xsltSource = new StreamSource(new File(
                "D:/git-repos/validator-configuration-xrechnung/build/resources/xrechnung/1.1/xsl/XRechnung-UBL-validation-Invoice.xsl"));

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
            log.error("Could not configure schematron cheker =" + schematron, e);
        }

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
            PrintStream out = new PrintStream("svrl.xml");

            XMLMutateApp.printDocument((Document) resultNode, out);
        } catch (IOException | TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
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