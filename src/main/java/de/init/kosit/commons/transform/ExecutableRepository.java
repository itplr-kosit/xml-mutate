// Generated by delombok at Fri Aug 13 16:11:22 CEST 2021
package de.init.kosit.commons.transform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import de.init.kosit.commons.CollectingErrorEventHandler;
import de.init.kosit.commons.artefact.Artefact;
import de.init.kosit.commons.util.CommonException;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Repository for loading and maintaining compiled XSLT executables. This implementation caches the
 *
 * @author Andreas Penski (]init[ AG)
 */
//@ApplicationScoped
public class ExecutableRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecutableRepository.class);
    private final Processor processor;
    private final Map<URI, XsltExecutable> executables = new HashMap<>();

    public ExecutableRepository(final Processor processor) {
        this.processor = processor;
    }

    private static Source resolve(final URI uri) {
        try {
            final URL resource = uri.toURL();
            return new StreamSource(resource.openStream(), resource.toURI().getRawPath());
        } catch (final IOException | URISyntaxException e) {
            throw new IllegalStateException("Can not load schema for resource " + uri, e);
        }
    }

    /**
     * Loads an XSLT executable based on the provided URI and resolver implementations. The generated
     *
     * @param uri the uri of the main executable script
     * @param resolver resolver for resolving script references
     * @return a maybe cached instance of the {@link XsltExecutable}
     */
    public XsltExecutable load(final URI uri, final URIResolver resolver) {
        return this.executables.computeIfAbsent(uri, k -> createExecutable(k, resolver));
    }

    /**
     * Loads an XSLT executable based on the provided URI and resolver implementations. This method may use caches /
     * previously created instance.
     *
     * @param transformationArtefact the uri of the main executable script
     * @param references some references neccessary for loading the executable
     * @return a cached instance of the {@link XsltExecutable}
     * @throws BusinessException when executable can not be created/loaded
     */
    public XsltExecutable load(final Artefact transformationArtefact, final Collection<Artefact> references) {
        XsltExecutable executable = this.executables.get(transformationArtefact.getUri());
        if (executable == null) {
            executable = createExecutable(transformationArtefact, references);
            if (transformationArtefact.getUri() != null) {
                this.executables.put(transformationArtefact.getUri(), executable);
            }
        }
        return executable;
    }

    /**
     * Creates a new XSLT executable based on the provided URI and resolver implementations.
     *
     * @param uri the uri of the main executable script
     * @param resolver resolver for resolving script references
     * @return a new instance of the {@link XsltExecutable}
     */
    public XsltExecutable createExecutable(final URI uri, final URIResolver resolver) {
        log.info("Loading XSLT script from  {}", uri);
        final XsltCompiler xsltCompiler = this.processor.newXsltCompiler();
        final CollectingErrorEventHandler listener = new CollectingErrorEventHandler();
        try {
            xsltCompiler.setErrorListener(listener);
            xsltCompiler.setURIResolver(resolver);
            return xsltCompiler.compile(resolve(uri));
        } catch (final SaxonApiException e) {
            listener.getErrors().forEach(event -> event.log(log));
            throw new IllegalStateException("Can not compile xslt executable for uri " + uri, e);
        } finally {
            if (!listener.hasErrors() && listener.hasEvents()) {
                log.warn("Received warnings while loading a xslt script {}", uri);
                listener.getErrors().forEach(e -> e.log(log));
            }
        }
    }

    /**
     * Creates a XSLT executable based on the provided URI and resolver implementations.
     *
     * @param transformationArtefact the uri of the main executable script
     * @param references some references neccessary for loading the executable
     * @return a cached instance of the {@link XsltExecutable}
     */
    public XsltExecutable createExecutable(final Artefact transformationArtefact, final Collection<Artefact> references) {
        try (InputStream xsl = new ByteArrayInputStream(transformationArtefact.getContent())) {
            final XsltCompiler xsltCompiler = this.processor.newXsltCompiler();
            xsltCompiler.setURIResolver(new InMemoryResolver(references));
            return xsltCompiler.compile(new StreamSource(xsl));
        } catch (final SaxonApiException | IOException e) {
            throw new CommonException(ErrorCode.COMPILE_ERROR, transformationArtefact.getUri());
        }
    }
}
