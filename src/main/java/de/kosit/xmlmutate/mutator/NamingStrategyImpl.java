package de.kosit.xmlmutate.mutator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * NamingStrategyImpl
 * @author Renzo Kottmann
 */
public class NamingStrategyImpl implements NamingStrategy {

    private final static Logger log = LogManager.getLogger(NamingStrategyImpl.class);

    private String sourceName = "unknown";
    private String name = "";

    public NamingStrategyImpl() {

    }

    public NamingStrategy byId(String sourceName, String id) {
        this.setSourceName(sourceName);
        this.name = this.normalize(sourceName) + "-" + this.normalize(id);
        return this;
    }

    private String normalize(String dirty) {
        if (sourceName != null && sourceName.isEmpty()) {
            throw new IllegalArgumentException("The soure name should be not null and not empty!");
        }
        return dirty.trim().toLowerCase().replace(" ", "-").replace("/", "_").replace("\\","_");
    }

    private void setSourceName(String sourceName) {
        if (sourceName != null && sourceName.isEmpty()) {
            throw new IllegalArgumentException("The soure name should be not null and not empty!");
        }

        this.sourceName = sourceName;
    }

    @Override
    public String getFileName() {
        return this.name + ".xml";
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSourceName() {
        return this.sourceName;
    }

}