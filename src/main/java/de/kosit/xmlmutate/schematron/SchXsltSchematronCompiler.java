package de.kosit.xmlmutate.schematron;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import name.dmaus.schxslt.Compiler;
import name.dmaus.schxslt.SchematronException;

/**
 * SchXsltSchematronCompiler
 */
public class SchXsltSchematronCompiler implements SchematronCompiler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchXsltSchematronCompiler.class);

    private Document compiledXSLT = null;

    @Override
    public List<String> extractRuleIds() {
        if (compiledXSLT == null) {
            log.error("No document loaded. Maybe compile first?", new IllegalStateException());
            return Collections.emptyList();
        }
        // compiledXSLT.getElementsByTagNameNS(SCHEMATRON_NS,
        // Evaluate XPath against Document itself
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext svrlContext = new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("svrl".equals(prefix)) {
                    return SVRL_NS_URI;
                }
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return null;
            }
        };
        xPath.setNamespaceContext(svrlContext);

        NodeList nodes = null;
        String xpathExpression = "//svrl:failed-assert/@id";
        try {

            nodes = (NodeList) xPath.evaluate(xpathExpression,
                    this.compiledXSLT, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Could not execute xpath=" + xpathExpression, e);
        }
        if (nodes == null || nodes.getLength() == 0) {
            log.error("there should be ids in the Schematron file!");
        }
        List<String> ids = new ArrayList<>();
        for (int i = 0; nodes != null && i < nodes.getLength(); ++i) {
            log.debug(nodes.item(i).getNodeValue());
            ids.add(nodes.item(i).getNodeValue());
        }
        log.debug("extracted ids" + ids.toString());
        return ids;
    }

    @Override
    public URI compile(Path target, URI schematronFile) {
        // FIX: Check if file is already compiled XSLT
        String extension = FilenameUtils.getExtension(schematronFile.getPath());

        // Handle pre-compiled XSLT files
        if (extension.equalsIgnoreCase("xsl") || extension.equalsIgnoreCase("xslt")) {
            log.debug("File {} is already compiled XSLT, skipping compilation", schematronFile);

            // Load the XSLT document for rule ID extraction
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                this.compiledXSLT = factory.newDocumentBuilder().parse(new File(schematronFile));
                log.debug("Successfully loaded pre-compiled XSLT from {}", schematronFile);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot load pre-compiled XSLT file= " + schematronFile, e);
            }

            return schematronFile;
        }

        // Only compile if it's a .sch file
        if (!extension.equalsIgnoreCase("sch")) {
            log.warn("Unknown file extension '{}' for file {}. Expected .sch, .xsl, or .xslt",
                    extension, schematronFile);
            log.warn("Attempting to process as schematron file anyway...");
        }
        // Proceed with compilation for .sch files
        log.debug("Compiling schematron file {} using SchXslt", schematronFile);

        Compiler schXsltcompiler = new Compiler();
        javax.xml.transform.Source source = new StreamSource(new File(schematronFile));
        // for now without any options
        Map<String, Object> options = Collections.emptyMap();
        try {
            this.compiledXSLT = schXsltcompiler.compile(source, options);

        } catch (SchematronException e) {
            throw new IllegalStateException("Cannot compile schematron file= " + schematronFile, e);
        }
        log.debug("Successfully compiled Schematron to SchemaDocument with systemid=" + compiledXSLT.getDocumentURI());
        try {
            return new URI(compiledXSLT.getDocumentURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could create uri from=" + compiledXSLT.getDocumentURI(), e);
        }
    }

}
