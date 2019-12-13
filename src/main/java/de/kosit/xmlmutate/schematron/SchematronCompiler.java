package de.kosit.xmlmutate.schematron;

import de.init.kosit.commons.ObjectFactory;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Schematron compiler from sch to xsl that uses the Skeleton-Implementation for
 * XSLT2 and the Saxon-Framework
 * http://schematron.com/front-page/the-schematron-skeleton-implementation/ The
 * compiler also extract the ids of the schematron rules
 *
 * @author Victor del Campo
 * @@author Renzo Kottmann
 */

public class SchematronCompiler {

    private static final Logger log = LoggerFactory.getLogger(SchematronCompiler.class);

    private static final String ISO_SCHEMATRON_FOLDER = "/iso-schematron-xslt2";
    private static final String ISO_SCHEMATRON_INCLUDE = ISO_SCHEMATRON_FOLDER + "/iso_dsdl_include.xsl";
    private static final String ISO_SCHEMATRON_EXPAND = ISO_SCHEMATRON_FOLDER + "/iso_abstract_expand.xsl";
    private static final String ISO_SCHEMATRON_COMPILE = "iso_svrl_for_xslt2.xsl";

    /**
     * Method that extract the ids of the schematron rules (of the compiled
     * schematron) and add them to the Java Schematron Object
     *
     * @param compiledSchematron - the URI of the compiled schematron
     * @return the list of the rule ids
     */
    public List<String> extractRuleIdList(final URI compiledSchematron) {

        log.debug("Extracting ids of schematron rules...");
        final List<String> rulesIds = new ArrayList<>();

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final XPath xPath = XPathFactory.newInstance().newXPath();
        final String expression = "//template/choose/otherwise//failed-assert/attribute[@name='id']/text()";

        Document document;
        NodeList rules;
        try {
            document = factory.newDocumentBuilder().parse(new File(compiledSchematron));
            rules = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new IllegalArgumentException("Ids of schematron rules could not be extracted");
        }

        for (int i = 0; i < rules.getLength(); i++) {
            final String value = rules.item(i).getNodeValue();
            rulesIds.add(value);
        }
        log.debug("{} rule ids extracted ", rulesIds.size());
        return rulesIds;
    }

    private Source compileISO(final Source schematron) {
        // Create processor and compiler
        final Processor processor = new Processor(false);
        Configuration saxonConfig = processor.getUnderlyingConfiguration();
        log.debug("parser class={}", saxonConfig.getSourceParserClass());
        log.debug("parser sourc={}", saxonConfig.getSourceParser());

        final XsltCompiler compiler = processor.newXsltCompiler();

        // Stage 1: expand inclusions with iso_dsdl_include.xsl
        final Source sourceStage2 = compileStage(compiler, schematron, ISO_SCHEMATRON_INCLUDE);

        // Stage 2: expand abstract patterns with iso_abstract_expand.xsl
        final Source sourceStage3 = compileStage(compiler, sourceStage2, ISO_SCHEMATRON_EXPAND);

        // Stage 3: svrl it with iso_svrl_for_xslt2.xsl
        final Source generatedXsltSource = compileStage(compiler, sourceStage3, ISO_SCHEMATRON_COMPILE);

        return generatedXsltSource;
    }

    /**
     * Method that compiles a Schematron file (file extension "sch") into a XSLT
     * file using the ISO Schematron implementations and going through the 3 stages:
     * include, abstract and compile
     *
     * @param schematronFile - the URI of the schematron file to be compiled
     * @return the URI of the compiled schematron
     */
    public URI compile(final Path schematronPath) {

        log.debug("Loading Schematron from path={}", schematronPath.toString());
        final String path = schematronPath.getParent().toString();
        final String baseName = schematronPath.getFileName().toString();
        // Prüfung ob sch file

        final String fileExtension = baseName.substring(baseName.lastIndexOf('.') + 1);
        // Get name for compiled schematron

        final String fileName = baseName.substring(0, baseName.lastIndexOf('.'));

        if (!fileExtension.equalsIgnoreCase("sch")) {
            return schematronPath.toUri();
        }
        log.debug("Schematron input needs to be compiled. Basename={} fileName={} extension={}", baseName, fileName,
                fileExtension);

        // Sch-file source
        final Source schematron = new StreamSource(schematronPath.toFile());

        final Source schXSLT = compileISO(schematron);

        // Generate file output
        final File schematronOutputFile = new File(path, fileName + ".xsl");
        writeXSLT(schXSLT, schematronOutputFile);

        log.debug("Schematron compilation completed");
        log.debug("Final XSLT location: {}", schematronOutputFile.toString());

        return schematronOutputFile.toURI();
    }

    private Source compileStage(final XsltCompiler compiler, final Source xmlSource, final String xsltPath) {

        log.debug("Compiling source={} with xslt={}", xmlSource.getSystemId(), xsltPath);
        final XdmDestination destStage = new XdmDestination();
        XdmNode node = null;
        InputStream input = this.getClass().getResourceAsStream(xsltPath);
        log.debug("Input from classpath={}", input.toString());
        StreamSource xsltSource = new StreamSource(input);
        xsltSource.setSystemId(xsltPath);
        // xsltSource.

        log.debug("xsltSource=", xsltSource);
        try {
            final Xslt30Transformer transformer = compiler.compile(xsltSource).load30();

            transformer.applyTemplates(xmlSource, destStage);
            node = destStage.getXdmNode();

        } catch (SaxonApiException e) {
            throw new IllegalArgumentException("Schematron file could not be compiled: " + e.getMessage());
        } catch (IllegalStateException ie) {
            throw new IllegalArgumentException(
                    "Illigal state: Schematron file could not be compiled: " + ie.getMessage());
        }
        return node.asSource();
    }

    private void writeXSLT(final Source xsltSource, final File xsltFile) {
        // Transform to file/URI
        final Transformer transformer = ObjectFactory.createTransformer(true);

        try {
            final Result result = new StreamResult(xsltFile);
            transformer.transform(xsltSource, result);

        } catch (final TransformerException e) {
            throw new IllegalArgumentException("Final Schematron file could not be written: " + e.getMessage());
        }
    }

}
