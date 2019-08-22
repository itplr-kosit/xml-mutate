package de.kosit.xmlmutate.mutation;

import java.net.URI;
import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Information about a Schematron file.
 *
 * @author Andreas Penski
 */
@Getter
@RequiredArgsConstructor
public class Schematron {

    /** Default-Name auf der CMD-Line kein eigener Name angegeben wurde */
    public static final String DEFAULT_NAME = "SCHEMATRON";

    /**
     * Symbolic name for this file of Schematron rules.
     */
    private final String name;

    /**
     * The URI of the Schematron file.
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
