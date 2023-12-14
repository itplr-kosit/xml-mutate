package de.kosit.xmlmutate.schematron;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public interface SchematronCompiler {

    static final String SCHEMATRON_NS_URI = "http://purl.oclc.org/dsdl/schematron";
    static final String SVRL_NS_URI = "http://purl.oclc.org/dsdl/svrl";

    /**
     * Extracts the ids of all assertions within a Schematron
     *
     * @return the list of the rule ids
     */
    List<String> extractRuleIds();

    /**
     * Method that compiles a Schematron file to an executable XML validator.
     *
     * @param targetDir       - the target directory to save compiledSchematron
     * @param soureSchematron - the URI of the Schematron file to be compiled
     * @return the URI of the compiled Schematron
     */
    URI compile(Path targetDir, URI sourceSchematron);

}