package de.kosit.xmlmutate.mutation;

import java.net.URI;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value-Objekt mit Informationen über eine Schematron-Datei
 * 
 * @author Andreas Penski
 */
@Getter
@RequiredArgsConstructor
public class Schematron {

    /** Default-Name auf der CMD-Line kein eigener Name angegeben wurde */
    public static final String DEFAULT_NAME = "SCHEMATRON";

    private final String name;

    private final URI uri;
}
