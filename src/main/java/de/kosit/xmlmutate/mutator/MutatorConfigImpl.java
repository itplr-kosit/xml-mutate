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

    private Map<String, String> config = null;

    MutatorConfigImpl() {
        config = new HashMap<String, String>();
    }

    public void addConfigItem(String name, String value) {
        config.put(name, value);
    }

    public String getConfigItem(String name) {
        return config.get(name);
    }

    @Override
    public void setInstructionName(String mutatorName) {
        if (mutatorName == null || "".equals(mutatorName)) {
            throw new IllegalArgumentException("Nutator name of xmute instruction can not be null!");
        }
        config.put("mutator", mutatorName.trim().toLowerCase());
    }

    @Override
    public String getInstructionName() {
        String name = config.get("mutator");
        if (name == null ) {
            throw new IllegalStateException("No mutator name of xmute instruction given");
        }
        return name;
    }

}