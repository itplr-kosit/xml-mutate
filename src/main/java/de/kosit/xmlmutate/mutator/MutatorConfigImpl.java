package de.kosit.xmlmutate.mutator;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MutatorConfigImpl
 * @author Renzo Kottmann
 */
public class MutatorConfigImpl implements MutatorConfig {

    private final static Logger log = LogManager.getLogger(MutatorConfigImpl.class);

    Map<String, String> config = null;

    MutatorConfigImpl() {
        config = new HashMap<String, String>();
    }

    public void addConfigItem(String name, String value) {
        config.put(name, value);
    }

    public String getConfigItem(String name) {
        return config.get(name);
    }

}