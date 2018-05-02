package de.kosit.xmlmutate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * XMLMutateCnfigurator
 */
public class XMLMutateConfigurator {
    private final static Logger log = LogManager.getLogger(XMLMutateConfigurator.class);
    private XMLMutateConfigurationImpl config = null;
    private List<Path> inputPathList = new ArrayList<Path>();

    public XMLMutateConfigurator() {
        // The absolute default config no env, no config files
        this.config = new XMLMutateConfigurationImpl();
    }

    /**
     * assumes files as arguments at the end only
     */
    public XMLMutateConfiguration fromCommandLine(String[] line) {
        if (line == null) {
            log.debug("No command line arguments give. Return default config");
            return config;
        }
        this.parseCommandLine(line);
        return config;
    }

    public List<Path> getInputPaths() {
        return inputPathList;
    }

    private void parseCommandLine(String[] line) {
        String arg = "";
        for (int i = 0; i < line.length; i++) {
            arg = line[i].toLowerCase();
            switch (arg) {
            case "--output-dir":
                config.setOutputDir(line[++i]);
                break;
            case "-m":
                config.setRunMode(line[++i]);
                break;
            case "--schema":
                String schemaName = line[++i];
                String schemaFile = line[++i];

                config.addSchema(schemaName, schemaFile);
                break;
            default:
                inputPathList.add(Paths.get(line[i]));
                break;
            }

        }
    }
}