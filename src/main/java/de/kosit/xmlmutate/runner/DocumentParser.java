package de.kosit.xmlmutate.runner;

import de.init.kosit.commons.ObjectFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parsing functionalities
 *
 * @author Andreas Penski
 */
@Slf4j
public class DocumentParser {

    private static class NamespaceMapping {

        @AllArgsConstructor
        @Getter
        @RequiredArgsConstructor
        private static class NS {

            private final String namespace;

            private final String prefix;

            @Setter
            private boolean declared;
        }

        private final Map<String, NS> namespaces = new HashMap<>();

        public void register(final String prefix, final String uri) {
            this.namespaces.put(uri, new NS(uri, prefix));
        }

        public boolean isAvailable(final String namespace) {
            return this.namespaces.containsKey(namespace);
        }

        public boolean isDeclared(final String namespace) {
            return isAvailable(namespace) && this.namespaces.get(namespace).isDeclared();
        }

        public void remove(final String prefix) {
        }

        public void setDeclared(final String uri) {
            if (isAvailable(uri) && !isDeclared(uri)) {
                this.namespaces.get(uri).setDeclared(true);
            } else {
                // throw new IllegalStateException("Can not be set declared");
            }
        }

        public List<NS> getUndeclared() {
            return this.namespaces.values().stream().filter(ns -> !ns.isDeclared()).collect(Collectors.toList());
        }
    }

    /**
     * Handler that identifies the row number for further processing
     */
    @RequiredArgsConstructor
    private static class PositionalHandler extends DefaultHandler2 {

        final Document doc;

        final Deque<Element> elementStack = new ArrayDeque<>();

        final StringBuilder textBuffer = new StringBuilder();

        private final NamespaceMapping namespaces = new NamespaceMapping();

        private Locator locator;

        @Override
        public void setDocumentLocator(final Locator locator) {
            this.locator = locator;
        }

        @Override
        public void comment(final char[] ch, final int start, final int length) {
            append(this.doc.createComment(new String(ch, start, length)));
        }

        @Override
        public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
            this.namespaces.register(prefix, uri);
        }

        @Override
        public void endPrefixMapping(final String prefix) throws SAXException {
            final Element current = this.elementStack.peek() != null ? this.elementStack.peek() : this.doc.getDocumentElement();
            this.namespaces.getUndeclared().forEach(ns -> {
                current.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + ns.getPrefix(), ns.getNamespace());
                ns.setDeclared(true);
            });
            this.namespaces.remove(prefix);
        }

        @Override
        public void processingInstruction(final String target, final String data) {
            addTextIfNeeded();
            append(this.doc.createProcessingInstruction(target, data));
        }

        private void append(final Node node) {
            node.setUserData(LINE_NUMBER_KEY_NAME, String.valueOf(this.locator.getLineNumber()), null);
            if (this.elementStack.isEmpty()) { // Is this the root element?
                this.doc.appendChild(node);
            } else {
                final Element parentEl = this.elementStack.peek();
                parentEl.appendChild(node);
            }
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
            addTextIfNeeded();
            final Element el;
            el = this.doc.createElementNS(uri, qName);
            for (int i = 0; i < attributes.getLength(); i++) {
                el.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i));
            }
            el.setUserData(LINE_NUMBER_KEY_NAME, String.valueOf(this.locator.getLineNumber()), null);
            this.elementStack.push(el);
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            addTextIfNeeded();
            append(this.elementStack.pop());
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) {
            this.textBuffer.append(ch, start, length);
        }

        // Outputs text accumulated under the current node
        private void addTextIfNeeded() {
            if (this.textBuffer.length() > 0) {
                final Element e1 = this.elementStack.peek();
                final Node textNode = this.doc.createTextNode(this.textBuffer.toString());
                e1.appendChild(textNode);
                this.textBuffer.delete(0, this.textBuffer.length());
            }
        }
    }

    /**
     * UserData-Key for the row number
     */
    public static final String LINE_NUMBER_KEY_NAME = "lineNumber";

    private DocumentParser() {
        // hide
    }

    /**
     * Default parsing functionality via SAX. This parser determines the row information for further use/localisation
     * within the same run
     *
     * @param path the document path
     * @return the document read
     */
    public static Document readDocument(final Path path) {

        try (final InputStream input = Files.newInputStream(path)) {
            return readDocument(input);
        } catch (final SAXException | IOException | ParserConfigurationException e) {
            log.error("Error opening document {}", path, e);
            throw new IllegalArgumentException("Can not open Document " + path, e);
        }
    }

    private static Document readDocument(final InputStream input) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilder builder = ObjectFactory.createDocumentBuilder(false);
        final Document d = builder.newDocument();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        final SAXParser saxParser = factory.newSAXParser();
        final PositionalHandler handler = new PositionalHandler(d);
        final XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        saxParser.parse(input, handler);
        return d;

    }

    public static Document readDocument(final String xml) {
        try (final InputStream input = new ByteArrayInputStream(xml.getBytes())) {
            return readDocument(input);
        } catch (final SAXException | IOException | ParserConfigurationException e) {
            log.error("Error opening document {}", xml, e);
            throw new IllegalArgumentException("Can not open Document from " + xml, e);
        }
    }

    /**
     * Implementation that reads the document as fast as possible without any further row reference
     *
     * @param path the path
     * @return the document read
     */
    public static Document readDocumentPlain(final Path path) {
        final DocumentBuilder builder = ObjectFactory.createDocumentBuilder(false);
        try (final InputStream input = Files.newInputStream(path)) {
            return builder.parse(input);
        } catch (final SAXException | IOException e) {
            log.error("Error opening document {}", path, e);
            throw new IllegalArgumentException("Can not open Document " + path, e);
        }
    }
}
