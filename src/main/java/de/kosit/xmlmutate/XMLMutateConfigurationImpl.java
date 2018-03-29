package de.kosit.xmlmutate;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * XMLMutateConfig
 */
public class XMLMutateConfigurationImpl implements XMLMutateConfiguration {

    private Path outputDir = null;
    private String runMode = null;

    public XMLMutateConfigurationImpl() {
        //TODO create defaults
        this.defaultOutputDir();
        this.runMode = "mutate";
    }

    private void defaultOutputDir() {
        outputDir = Paths.get(System.getProperty("user.home")).normalize().toAbsolutePath();
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String dirName) {
        if (dirName == null) {
            throw new IllegalArgumentException("Output directory name should be given");
        }
        this.outputDir = Paths.get(dirName);
    }

    /**
	 * @return the runMode
	 */
    public String getRunMode() {
        return this.runMode;
    }

	/**
	 * @param runMode the runMode to set
	 */
	public void setRunMode(String runMode) {
		this.runMode = runMode;
	}

}