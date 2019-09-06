package de.kosit.xmlmutate.mutation;

/**
 * @author Andreas Penski
 */
public interface FileNameGenerator {

    String generateName(String docName, String postfix);

    String generateName();
}
