// Generated by delombok at Fri Aug 13 16:11:22 CEST 2021
package de.init.kosit.commons.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.init.kosit.commons.CollectingErrorEventHandler;
import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.Result;
import de.init.kosit.commons.Severity;
import de.init.kosit.commons.SyntaxError;

/**
 * JAXB Conversion Utility.
 */
// @ApplicationScoped
public class ConversionService {
    @java.lang.SuppressWarnings("all")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConversionService.class);


    /**
     * Exception while serializing/deserializing with jaxb.
     */
    public class ConversionExeption extends RuntimeException {
        /**
         * Constructor.
         *
         * @param message the message.
         * @param cause the cause
         */
        public ConversionExeption(final String message, final Exception cause) {
            super(message, cause);
        }

        /**
         * Constructor.
         *
         * @param message the message.
         */
        public ConversionExeption(final String message) {
            super(message);
        }
    }

    private static final int MAX_LOG_CONTENT = 50;
    // context setup
    private JAXBContext jaxbContext;

    private static <T> QName createQName(final T model) {
        return new QName(model.getClass().getSimpleName().toLowerCase());
    }

    public ConversionService () {
    }

    private void checkInputEmpty(final byte[] xml) {
        if (ArrayUtils.isEmpty(xml)) {
            throw new ConversionExeption("Can not unmarshal empty input");
        }
    }

    private <T> void checkTypeEmpty(final Class<T> type) {
        if (type == null) {
            throw new ConversionExeption("Can not unmarshal without type information. Need to specify a target type");
        }
    }

    /**
     * Initialsiert den conversion service mit dem angegebenen Kontextpfad.
     *
     * @param contextPath der Kontextpfad
     */
    public void initialize(final String contextPath) {
        try {
            this.jaxbContext = JAXBContext.newInstance(contextPath);
        } catch (final JAXBException e) {
            throw new IllegalStateException(String.format("Can not create JAXB context for given context: %s", contextPath), e);
        }
    }

    public JAXBContext getJaxbContext() {
        if (this.jaxbContext == null) {
            throw new IllegalStateException ("JAXB Context was never initialized");
        }
        return this.jaxbContext;
    }

    /**
     * Unmarshalls a specifc xml model into a defined java object.
     *
     * @param xml the xml
     * @param type the expected type created
     * @param <T> type information
     * @return the created object
     */
    public <T> T readXml(final byte[] xml, final Class<T> type) {
        return readXml(xml, type, null);
    }

    /**
     * Unmarshalls a specifc xml model into a defined java object.
     *
     * @param xml the xml
     * @param type the expected type created
     * @param listener an optional unmarshaller listener to use
     * @param <T> type information
     * @return the created object
     */
    public <T> T readXml(final byte[] xml, final Class<T> type, final Unmarshaller.Listener listener) {
        checkInputEmpty(xml);
        checkTypeEmpty(type);
        try (ByteArrayInputStream in = new ByteArrayInputStream(xml)) {
            final StreamSource source = new StreamSource(in);
            return readXml(source, type, listener);
        } catch (final IOException e) {
            throw new ConversionExeption(String.format("Can not unmarshal to type %s", type.getSimpleName()), e);
        }
    }

    /**
     * Unmarshalls a specific xml model into a defined java object.
     *
     * @param source the xml
     * @param type the expected type created
     * @param <T> type information
     * @return the created object
     */
    public <T> T readXml(final URI source, final Class<T> type) {
        return readXml(source, type, null);
    }

    /**
     * Unmarshalls a specifc xml model into a defined java object.
     *
     * @param source the xml
     * @param type the expected type created
     * @param listener an optional unmarshaller listener to use
     * @param <T> type information
     * @return the created object
     */
    public <T> T readXml(final URI source, final Class<T> type, final Listener listener) {
        try (InputStream in = source.toURL().openStream()) {
            return readXml(new StreamSource(in), type, listener);
        } catch (final IOException e) {
            throw new ConversionExeption(String.format("Can not unmarshal to type %s. Give xml starts with %s", type.getSimpleName(), StringUtils.abbreviate(source.toString(), MAX_LOG_CONTENT)), e);
        }
    }

    /**
     * Unmarshalls a specifc xml model into a defined java object.
     *
     * @param source the source xml
     * @param type the expected type created
     * @param listener an optional unmarshaller listener to use
     * @param <T> type information
     * @return the created object
     */
    public <T> T readXml(final Source source, final Class<T> type, final Listener listener) {
        try {
            final Unmarshaller u = getJaxbContext().createUnmarshaller();
            if (listener != null && StreamAccess.class.isAssignableFrom(listener.getClass()) && StreamSource.class.isAssignableFrom(source.getClass())) {
                final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
                final XMLStreamReader xsr = xmlInputFactory.createXMLStreamReader(source);
                ((StreamAccess) listener).setStreamReader(xsr);
                u.setListener(listener);
                return u.unmarshal(xsr, type).getValue();
            } else {
                u.setListener(listener);
                return u.unmarshal(source, type).getValue();
            }
        } catch (final JAXBException | XMLStreamException e) {
            throw new ConversionExeption(String.format("Can not unmarshal to type %s: %s", type.getSimpleName(), StringUtils.abbreviate(source.getSystemId(), MAX_LOG_CONTENT)), e);
        }
    }

    /**
     * Serializing an object to xml string.
     *
     * @param model the object
     * @param <T> type of the object
     * @return the serialized form.
     */
    public <T> String toString(final T model) {
        return new String(writeXml(model, true), StandardCharsets.UTF_8);
    }

    /**
     * Serializing an object to xml.
     *
     * @param model the object
     * @param <T> type of the object
     * @return the serialized form.
     */
    public <T> byte[] writeXml(final T model) {
        return writeXml(model, false);
    }

    /**
     * Serializing an object to xml.
     *
     * @param model the object
     * @param fragment defines whether the result is a fragment
     *
     * @param <T> type of the object
     * @return the serialized form.
     */
    public <T> byte[] writeXml(final T model, final boolean fragment) {
        if (model == null) {
            throw new ConversionExeption("Can not serialize null");
        }
        try (ByteArrayOutputStream w = new ByteArrayOutputStream()) {
            final JAXBIntrospector introspector = getJaxbContext().createJAXBIntrospector();
            final Marshaller marshaller = getJaxbContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            if (null == introspector.getElementName(model)) {
                final JAXBElement jaxbElement = new JAXBElement(createQName(model), model.getClass(), model);
                marshaller.marshal(jaxbElement, w);
            } else {
                marshaller.marshal(model, w);
            }
            w.flush();
            return w.toByteArray();
        } catch (JAXBException | IOException e) {
            throw new ConversionExeption(String.format("Error serializing Object %s", model.getClass().getName()), e);
        }
    }

    /**
     * Parsed und überprüft ein übergebenes Dokument darauf ob es well-formed ist. Dies stellt den ersten
     * Verarbeitungsschritt des Prüf-Tools dar. Diese Funktion verzichtet explizit auf das Validieren gegenüber ein Schema.
     *
     * @param content ein Dokument
     * @return Ergebnis des Parsings inklusive etwaiger Fehler
     */
    public Result<Document, SyntaxError> parseDocument(final InputStream content) {
        if (content == null) {
            throw new IllegalArgumentException("Url may not be null");
        }
        Result<Document, SyntaxError> result;
        final CollectingErrorEventHandler errorHandler = new CollectingErrorEventHandler();
        try {
            final DocumentBuilder db =
                // BeanProvider.getDependent(DocumentBuilder.class, AnnotationInstanceProvider.of(NonValidatating.class)).get();
                ObjectFactory.createNonValidatingDocumentBuilder ();
            db.setErrorHandler(errorHandler);
            final Document doc = db.parse(content);
            result = new Result<>(doc, errorHandler.getErrors());
        } catch (final SAXException e) {
            log.debug("SAXException while parsing {}", content, e);
            result = new Result<>(errorHandler.getErrors());
        } catch (final IOException e) {
            log.debug("IOException while parsing {}", content, e);
            final SyntaxError error = new SyntaxError();
            error.setSeverity(Severity.SEVERITY_FATAL_ERROR);
            error.setMessage(String.format("IOException while reading resource %s", content));
            result = new Result<>(Collections.singleton(error));
        }
        return result;
    }

    public Result<Document, SyntaxError> parseDocument(final byte[] content) {
        try (InputStream input = new ByteArrayInputStream(content)) {
            return parseDocument(input);
        } catch (final IOException e) {
            log.debug("IOException while parsing {}", content, e);
            final SyntaxError error = new SyntaxError();
            error.setSeverity(Severity.SEVERITY_FATAL_ERROR);
            error.setMessage(String.format("IOException while reading resource %s", content));
            return new Result<>(Collections.singleton(error));
        }
    }

    /**
     * Initializes the context with a specific instance.
     *
     * @param context the context
     */
    public void initialize(final JAXBContext context) {
        this.jaxbContext = context;
    }
}
