package de.kosit.xmlmutate;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import de.kosit.xmlmutate.mutator.MutatorException;

/**
 * XMLMutateConfig
 */
public class XMLMutateConfigurationImpl implements XMLMutateConfiguration {

    private Path outputDir = null;
    private RunModeEnum runMode = null;
    private Map<String, Schema> xsdCache = null;

    public XMLMutateConfigurationImpl() {
        this.defaultOutputDir();
        this.runMode = RunModeEnum.GENERATE;
        this.xsdCache = new HashMap<String, Schema>();
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

    public void addSchema(String schemaName, String schemaFile) {
        Objects.requireNonNull(schemaName, "schemaName should not be null");
        Objects.requireNonNull(schemaFile, "schemaFile should not be null");

        if (schemaName.isEmpty() ) {
            throw new IllegalArgumentException("schemaName should not be empty");
        }

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = schemaFactory.newSchema(new File(schemaFile));
        } catch (SAXException e) {
            throw new MutatorException("Could not parse Schema file=" + schemaFile, e);
        }
        xsdCache.put(schemaName, schema);
    }

    @Override
    public Schema getSchema(String schemaName) {
        return xsdCache.get(schemaName);
    }

    @Override
    public boolean hasSchema(String schemaName) {
        return xsdCache.containsKey(schemaName);
    }

    @Override
    public boolean hasSchema() {
        return ! xsdCache.isEmpty();
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