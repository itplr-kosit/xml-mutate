package de.kosit.xmlmutate.mutator;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Hilfsklasse zum Generieren von Texten.
 * 
 * @author Andreas Penski
 */
public class TextGenerator {

    public String generateAlphaNumeric(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}
