package de.kosit.xmlmutate.mutation;

/**
 * @author Andreas Penski
 */
public interface NameGenerator {

    String generateName(String inputFileName, String mutatorName, String postfix);

}
