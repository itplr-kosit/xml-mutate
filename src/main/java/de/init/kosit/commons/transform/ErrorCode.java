// Generated by delombok at Fri Aug 13 16:11:22 CEST 2021
package de.init.kosit.commons.transform;

import de.init.kosit.commons.util.NamedError;

/**
 * @author apenski
 */
public enum ErrorCode implements NamedError {
    SCHEMATRON_VALIDIERUNG_ERROR("Fehler bei der Validierung mittels Schematron"), TRANSFORMATION_ERROR("Fehler beim Ausführen einer Transformation: {0}"), COMPILE_ERROR("Fehler beim kompilieren des XSLT-Scripts {0}");
    private final String template;

    @java.lang.SuppressWarnings("all")
    private ErrorCode(final String template) {
        this.template = template;
    }

    @java.lang.SuppressWarnings("all")
    public String getTemplate() {
        return this.template;
    }
}
