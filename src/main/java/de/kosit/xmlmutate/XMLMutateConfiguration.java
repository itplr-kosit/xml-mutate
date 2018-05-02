package de.kosit.xmlmutate;

import java.nio.file.Path;

import javax.xml.validation.Schema;

/**
 * XMLMutateConfiguration
 */
public interface XMLMutateConfiguration {

    public Path getOutputDir();

    public RunModeEnum getRunMode();

    public Schema getSchema(String schemaName);

    public boolean hasSchema(String schemaName);

    public boolean hasSchema();

}