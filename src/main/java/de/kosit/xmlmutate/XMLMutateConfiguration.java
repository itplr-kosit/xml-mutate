package de.kosit.xmlmutate;

import java.nio.file.Path;
import java.util.Map;

import javax.xml.validation.Schema;

import de.kosit.xmlmutate.tester.SchematronTester;

/**
 * XMLMutateConfiguration
 */
public interface XMLMutateConfiguration {

    public Path getOutputDir();

    public RunModeEnum getRunMode();

    public Schema getSchema(String schemaName);

    public boolean hasSchema(String schemaName);

    public boolean hasSchema();

    public Map<String, SchematronTester> getAllSchematronTester();

    public SchematronTester getSchematronTester(String schemaName);

    public boolean hasSchematron(String schemaName);

    public boolean hasSchematron();

}