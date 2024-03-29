// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
package de.kosit.xmlmutate.mutation;

import java.net.URI;

/**
 * Data objects holding information about xsl templates.
 *
 * @author Andreas Penski
 */
public class NamedTemplate {
    private final String name;
    private final URI path;

    public NamedTemplate(final String name, final URI path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public URI getPath() {
        return this.path;
    }
}
