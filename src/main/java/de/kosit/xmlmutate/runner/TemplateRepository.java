package de.kosit.xmlmutate.runner;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.collections4.map.HashedMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Repository for caching an XSLT Template for reuse.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class TemplateRepository {

    private final TransformerFactory factory;

    private final Map<String, Templates> templates = new HashedMap<>();

    /**
     * Constructor.
     */
    public TemplateRepository() {
        try {
            this.factory = TransformerFactory.newInstance();
            this.factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (final TransformerConfigurationException e) {
            throw new IllegalStateException("Can not prepare Template repository.", e);
        }
    }

    /**
     * Determine whether a named template exists or not.
     * 
     * @param name the name of the template
     * @return true if existing
     */
    public boolean exists(final String name) {
        return getTemplate(name) != null;
    }

    /**
     * Fetch a template from the cache.
     * 
     * @param name the name of the template
     * @return the compiled template or null if it is not existing
     */
    public Templates getTemplate(final String name) {
        return this.templates.get(name);
    }

    /**
     * Register a new template and try to compile it for further usage.
     * 
     * @param name the name of the template
     * @param uri the uri of the resource
     */
    public void registerTemplate(final String name, final URL uri) {
        checkArguments(name, uri);
        try ( final InputStream in = uri.openStream() ) {
            final Templates t = this.factory.newTemplates(new StreamSource(in));

            this.templates.put(name, t);
        } catch (final IOException | TransformerConfigurationException e) {
            throw new IllegalArgumentException(String.format("Can not compile xslt template %s from %s", name, uri.toString()));
        }
    }

    private void checkArguments(final String name, final Object uri) {
        if (isEmpty(name) || uri == null) {
            throw new IllegalArgumentException("Can not register template. Unsufficient information supplied");
        }
        if (this.templates.get(name) != null) {
            throw new IllegalArgumentException("Can not register template twice: " + name);
        }
    }

    /**
     * Register a new template and try to compile it for further usage.
     *
     * @param name the name of the template
     * @param uri the uri of the resource
     */
    public void registerTemplate(final String name, final URI uri) {
        checkArguments(name, uri);
        try {
            registerTemplate(name, uri.toURL());
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Can not compile xslt template %s from %s", name, uri.toString()));
        }
    }

    /**
     * Register a new template and try to compile it for further usage.
     * 
     * @param name the name of the template
     * @param path the {@link Path} to the resource
     */
    public void registerTemplate(final String name, final Path path) {
        registerTemplate(name, path.toUri());
    }

}
