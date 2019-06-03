package de.kosit.xmlmutate.mutation;

import java.net.URI;
import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Datenobjekt mit Informationen über eine Schematron-Datei
 * 
 * @author Andreas Penski
 */
@Getter
@RequiredArgsConstructor
public class Schematron {

    /** Default-Name auf der CMD-Line kein eigener Name angegeben wurde */
    public static final String DEFAULT_NAME = "SCHEMATRON";

    /**
     * Der (Kurz-)Name des Schematron-Regelsets
     */
    private final String name;

    /**
     * Die URI des Schematron-Scriptes.
     */
    private final URI uri;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Schematron that = (Schematron) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }
}
