package de.kosit.xmlmutate.mutator;

/*
 * NamingStrategy
 *
 * @author Renzo Kottmann
 */
public interface NamingStrategy {

    /**
     * Generates a Naming Strategy based on the original source name and an identifier.
     *
     * Form: "${basename}-${id}"
     */
    public NamingStrategy byId(String sourceName, String id);

    /**
     * Gives a plain name according to the choosen strategy.
     */
    public String getName();

    /**
     * Gives a name with ".xml" file extension as suffix
     */
    public String getFileName();

    public String getSourceName();

}