package de.kosit.xmlmutate.mutation;

import java.nio.file.Path;

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

    private final Path path;
}
