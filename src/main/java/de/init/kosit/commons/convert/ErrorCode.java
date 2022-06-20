// Generated by delombok at Fri Aug 13 16:11:22 CEST 2021
package de.init.kosit.commons.convert;

import de.init.kosit.commons.util.NamedError;

/**
 * Fehler im Zusammenhang mit Parsing
 *
 * @author Andreas Penski (]init[ AG)
 */
public enum ErrorCode implements NamedError {
    UNSUPPORTED_MAJOR_VERSION("Unerwartetet Major Version gefunden {0}. Erwartet wurde eine Instanz von {1} mit Framework-Version {2}"), UNSUPPORTED_NAMESPACE("Unerwartete Namespace-Deklaration \'{0}\' gefunden. Erwartet wurde eine Instanz von \'{1}\'"), GENERAL_PARSING_ERROR("Beim parsen des XML Dokuments {0} sind Fehler auftreten: {1}"), GENERAL_IO_ERROR("Es ist ein Fehler bei der Verarbeitung von {0} aufgetreten"), /**
     * Das Dokument hat die korrekte Version, kann aber nicht validiert werden
     */
    SCHEMA_MISMATCH("Das Dokument {0} wurde nicht erfolgreich validiert: {1}"), SCHEMA_MISMATCH_UPGRADE("Das Dokument {0} hat eine neuere Version und ist nicht kompatibel. {1}");
    private final String template;

    ErrorCode(final String template) {
        this.template = template;
    }

    public String getTemplate() {
        return this.template;
    }
}
