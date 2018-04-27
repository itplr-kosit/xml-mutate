package de.kosit.xmlmutate;
import java.nio.file.Path;

/**
 * XMLMutateConfiguration
 */
public interface XMLMutateConfiguration {
    public Path getOutputDir();
    public RunModeEnum getRunMode();
}