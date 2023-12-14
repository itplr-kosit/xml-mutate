package de.kosit.xmlmutate.schematron;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.init.kosit.commons.ObjectFactory;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;

/**
 * Schematron compiler from sch to xsl that uses the Skeleton-Implementation for
 * XSLT2 and the Saxon-Framework
 * http://schematron.com/front-page/the-schematron-skeleton-implementation/ The
 * compiler also extracts the ids of the schematron rules
 *
 * @author Renzo Kottmann
 * @author Victor del Campo
 */
public class ISOSchematronCompiler implements SchematronCompiler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ISOSchematronCompiler.class);
    private static final String ISO_SCHEMATRON_FOLDER = "/iso-schematron-xslt2";
    private static final String ISO_SCHEMATRON_INCLUDE = ISO_SCHEMATRON_FOLDER + "/iso_dsdl_include.xsl";
    private static final String ISO_SCHEMATRON_EXPAND = ISO_SCHEMATRON_FOLDER + "/iso_abstract_expand.xsl";
    private static final String ISO_SCHEMATRON_COMPILE = ISO_SCHEMATRON_FOLDER + "/iso_svrl_for_xslt2.xsl";
    private static final String ISO_SCHEMATRON_SAXON = ISO_SCHEMATRON_FOLDER + "/iso_schematron_skeleton_for_saxon.xsl";
    private static final String OUTPUT_FOLDER = File.separator + "xslt";

    private URI compiledXSLT = null;

    /**
     * Extracts the ids of the Schematron rules (of the compiled
     * Schematron) and adds them to the Java Schematron Object
     *
     * @param compiledSchematron - the URI of the compiled schematron
     * @return the list of the rule ids
     */
    @Override
    public List<String> extractRuleIds() {
        if (compiledXSLT == null) {
            log.error("No document loaded. Maybe compile first?", new IllegalStateException());
            return Collections.emptyList();
        }
        log.debug("Extracting ids of schematron rules at {}", compiledXSLT.toString());
        final List<String> rulesIds = new ArrayList<>();
        System.setProperty("javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON,
                "net.sf.saxon.xpath.XPathFactoryImpl");
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        XPath xPath = null;
        try {
            xPath = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON).newXPath();
        } catch (final XPathFactoryConfigurationException e) {
            throw new IllegalArgumentException("Can not use saxon for xpath2" + e.getLocalizedMessage());
        }
        final String expression = "//*:failed-assert/@id";
        Document document;
        NodeList rules;
        try {
            document = factory.newDocumentBuilder().parse(new File(compiledXSLT));
            rules = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new IllegalArgumentException(
                    "Ids of schematron rules could not be extracted. " + e.getLocalizedMessage());
        }
        for (int i = 0; i < rules.getLength(); i++) {
            final String value = rules.item(i).getNodeValue();
            rulesIds.add(value);
        }
        log.debug("{} rule ids extracted ", rulesIds.size());
        return rulesIds;
    }

    /**
     * Method that compiles a sch file into a xsl file using the ISO schematron
     * implementations and going through the 3 stages: include, abstract and compile
     *
     * @param target         - the RunnerConfig-Target location for all xslt
     *                       documents
     * @param schematronFile - the URI of the schematron file to be compiled
     * @return the URI of the compiled schematron
     */
    @Override
    public URI compile(final Path target, final URI schematronFile) {

        // Prüfung ob sch file
        if (!FilenameUtils.getExtension(schematronFile.getPath()).equalsIgnoreCase("sch")) {
            return schematronFile;
        }
        log.debug("Schematron input needs to be compiled...");
        log.debug("Loading XSLT script from {}", schematronFile.getPath());
        // Create processor and compiler
        final Processor processor = new Processor(false);
        final XsltCompiler compiler = processor.newXsltCompiler();
        // Sch-file source
        final Source sourceStage1 = new StreamSource(new File(schematronFile));
        // Stage 1: preprocess because of sch with separate parts
        final Source sourceStage2 = runStage(1, compiler, sourceStage1, ISO_SCHEMATRON_INCLUDE);
        // Stage 2: preprocess because of sch with abstract patterns
        final Source sourceStage3 = runStage(2, compiler, sourceStage2, ISO_SCHEMATRON_EXPAND);
        // Stage 3: compile sch to xsl/xslt
        final Source generatedXsltSource = runStage(3, compiler, sourceStage3, ISO_SCHEMATRON_COMPILE);
        // Generate file output
        final File outputFile = createFileOutput(generatedXsltSource,
                FilenameUtils.getBaseName(schematronFile.getPath()), target);
        log.debug("Schematron compilation completed");
        log.debug("XSLT location: {}", outputFile.getPath());
        this.compiledXSLT = outputFile.toURI();
        return outputFile.toURI();
    }

    private Source runStage(final int runnr, final XsltCompiler compiler, final Source source, final String path) {
        final XdmDestination destStage = new XdmDestination();
        try {
            if (runnr == 3) {
                compiler.setURIResolver(new URIResolver() {
                    @Override
                    public Source resolve(final String href, final String base) {
                        return new StreamSource(this.getClass().getResourceAsStream(ISO_SCHEMATRON_SAXON));
                    }
                });
            }
            log.info("Path of file to read: " + path);
            final Xslt30Transformer transformer = compiler
                    .compile(new StreamSource(this.getClass().getResourceAsStream(path))).load30();
            transformer.applyTemplates(source, destStage);
        } catch (final SaxonApiException e) {
            throw new IllegalArgumentException(runnr + ". Run: Schematron file could not be compiled");
        }
        log.info("Stage " + runnr + " completed successfully");
        return destStage.getXdmNode().asSource();
    }

    private File createFileOutput(final Source generatedXsltSource, final String fileNameBase, final Path target) {
        // Get name for compiled schematron from sch-file
        final String fileName = fileNameBase + ".xsl";
        // Transform to file/URI
        final Transformer transformer = ObjectFactory.createTransformer(true);
        final File compiledFile = new File(target.toString() + OUTPUT_FOLDER, fileName);
        try {
            transformer.transform(generatedXsltSource, new StreamResult(compiledFile));
        } catch (final TransformerException e) {
            throw new IllegalArgumentException("Schematron file could not be compiled");
        }
        return compiledFile;
    }
}
