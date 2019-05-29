package de.kosit.xmlmutate.runner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.init.kosit.commons.ObjectFactory;

/**
 * Parsing-Funktionalitäten.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class DocumentParser {

    /**
     * Handler, der die Zeilennummer für die weitere Verarbeitung ermittelt.
     */
    @RequiredArgsConstructor
    private static class PositionalHandler extends DefaultHandler2 {

        final Document doc;

        final Deque<Element> elementStack = new ArrayDeque<>();

        final StringBuilder textBuffer = new StringBuilder();

        private Locator locator;

        @Override
        public void setDocumentLocator(final Locator locator) {
            this.locator = locator;
        }

        @Override
        public void comment(final char[] ch, final int start, final int length) {
            append(this.doc.createComment(new String(ch)));
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
            final Element el = this.doc.createElement(qName);
            for (int i = 0; i < attributes.getLength(); i++) {
                el.setAttribute(attributes.getQName(i), attributes.getValue(i));
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
     * UserData-Key für die Zeilennummer.
     */
    public static final String LINE_NUMBER_KEY_NAME = "lineNumber";

    private DocumentParser() {
        // hide
    }

    /**
     * Default-Parsing-Funktionalität via SAX. Dieser Parser ermittelt Zeileninformationen zur weiteren
     * Verwendung/Lokalisierung innerhalb Durchlaufs.
     * 
     * @param path der Pfad des Dokuments.
     * @return das eingelesene Dokument
     */
    public static Document readDocument(final Path path) {
        final DocumentBuilder builder = ObjectFactory.createDocumentBuilder(false);
        try ( final InputStream input = Files.newInputStream(path) ) {
            final Document d = builder.newDocument();
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            final SAXParser saxParser = factory.newSAXParser();
            final PositionalHandler handler = new PositionalHandler(d);
            final XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            saxParser.parse(input, handler);
            return d;
        } catch (final SAXException | IOException | ParserConfigurationException e) {
            log.error("Error opening document {}", path, e);
            throw new IllegalArgumentException("Can not open Document " + path, e);
        }
    }

    /**
     * Implementierung, welche das Dokument möglichst schnell, ohne weiteren Zeilenbezug o.ä. einliest
     * 
     * @param path der Pfad
     * @return das eingelesene Dokument
     */
    public static Document readDocumentPlain(final Path path) {
        final DocumentBuilder builder = ObjectFactory.createDocumentBuilder(false);
        try ( final InputStream input = Files.newInputStream(path) ) {
            return builder.parse(input);
        } catch (final SAXException | IOException e) {
            log.error("Error opening document {}", path, e);
            throw new IllegalArgumentException("Can not open Document " + path, e);
        }
    }
}
