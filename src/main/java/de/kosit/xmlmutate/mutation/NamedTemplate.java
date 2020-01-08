package de.kosit.xmlmutate.mutation;

import java.net.URI;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data objects holding information about xsl templates.
 * 
 * @author Andreas Penski
 */
@Getter
@RequiredArgsConstructor
public class NamedTemplate {

    private final String name;

    private final URI path;
}
