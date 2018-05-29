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
import de.kosit.xmlmutate.tester.SchematronTester;

/**
 * XMLMutateConfig
 */
public class XMLMutateConfigurationImpl implements XMLMutateConfiguration {

    private Path outputDir = null;
    private RunModeEnum runMode = null;
    private Map<String, Schema> xsdCache = null;
    private Map<String, SchematronTester> schematronCache = null;

    public XMLMutateConfigurationImpl() {
        this.defaultOutputDir();
        this.runMode = RunModeEnum.GENERATE;
        this.xsdCache = new HashMap<String, Schema>();
        this.schematronCache = new HashMap<String, SchematronTester>();
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

        if (schemaName.isEmpty()) {
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

    public void addSchematron(String schematronName, String schematronFile) {
        Objects.requireNonNull(schematronName, "schematronName should not be null");
        Objects.requireNonNull(schematronFile, "schematronFile should not be null");

        if (schematronName.isEmpty()) {
            throw new IllegalArgumentException("schematronName should not be empty");
        }

        if (schematronFile.isEmpty()) {
            throw new IllegalArgumentException("schematronFile should not be empty");
        }
        SchematronTester st = new SchematronTester(schematronName, schematronFile);

        schematronCache.put(schematronName, st);
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
        return !xsdCache.isEmpty();
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

    @Override
    public SchematronTester getSchematronTester(String schematronName) {
        return schematronCache.get(schematronName);
    }

    @Override
    public boolean hasSchematron(String schematronName) {
        return schematronCache.containsKey(schematronName);
    }

    @Override
    public boolean hasSchematron() {
        return !schematronCache.isEmpty();
    }

    @Override
    public Map<String, SchematronTester> getAllSchematronTester() {
        return schematronCache;
    }

}