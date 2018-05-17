package de.kosit.xmlmutate;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.kosit.xmlmutate.XMLMutateException.Status;

/**
 * XMLMutateManufactory
 */
public class XMLMutateManufactory {

    public static String fileFromClasspath(String file) {
        // "D:/git-repos/xml-mutator/target/test-classes/ubl-invoice-empty-mutation-tests.xml",
        // "D:/git-repos/xml-mutator/target/test-classes/ubl-invoice-remove-mutation-tests.xml",
        // "D:/git-repos/xml-mutator/src/test/resources/ubl-invoice-add-mutation-tests.xml"
        // full file path to test xml instance although it is on classpath
        if (Objects.isNull(file)) {
            throw new XMLMutateException("File is null", XMLMutateException.Status.FILE_ERROR);
        }
        if ("".equals(file)) {
            throw new XMLMutateException("Filename is an empty string", XMLMutateException.Status.FILE_ERROR);
        }
        Class<? extends Object> clazz = XMLMutateManufactory.class;
        if (!file.startsWith("/")) {
            file = "/" + file;
        }
        URL url = clazz.getResource(file);
        if (Objects.isNull(url)) {
            throw new XMLMutateException(String.format("File=%s is not in classpath", file), Status.FILE_ERROR);
        }
        return url.getFile().replaceFirst("/", "");
    }

    public static Document domDocumentFromPath(Path file) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(true);
        docFactory.setNamespaceAware(true);
        docFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XMLMutateException("Could not create DOM Parser Factory", XMLMutateException.Status.PARSER_ERROR,
                    e);
        }
        Document doc = null;
        try {
            doc = docBuilder.parse(Files.newInputStream(file));
        } catch (IOException ioe) {
            throw new XMLMutateException("Could not read file=" + file.toString(), XMLMutateException.Status.FILE_ERROR,
                    ioe);
        } catch (SAXException e) {
            throw new XMLMutateException("Could not parse file=" + file, XMLMutateException.Status.PARSER_ERROR, e);
        }
        return doc;
    }

    public static Document domDocumentFromFileName(String fileName) {
        Path file = Paths.get(fileName);
        return domDocumentFromPath(file);
    }

}