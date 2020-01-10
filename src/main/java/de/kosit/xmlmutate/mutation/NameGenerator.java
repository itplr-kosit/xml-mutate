package de.kosit.xmlmutate.mutation;

/**
 * @author Andreas Penski
 */
public interface NameGenerator {

    String generateName(String docName, String postfix);

    String generateName(String docName);

    String generateName();
}
