package de.kosit.xmlmutate;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * XMLMutateConfig
 */
public class XMLMutateConfigurationImpl implements XMLMutateConfiguration {

    private Path outputDir = null;
    private RunModeEnum runMode = null;

    public XMLMutateConfigurationImpl() {
        this.defaultOutputDir();
        this.runMode = RunModeEnum.GENERATE;
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
    public RunModeEnum getRunMode() {
        return this.runMode;
    }

    /**
     * @param runMode the runMode to set
     */
    public void setRunMode(String runMode) {
        this.runMode = RunModeEnum.valueOf(runMode.toUpperCase());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();
        sb.append("XML MutaTe Configuration:").append(nl);
        sb.append("run.mode=").append(this.getRunMode()).append(nl);
        sb.append("output.dir=").append(this.getOutputDir()).append(nl);
        return sb.toString();
    }

}