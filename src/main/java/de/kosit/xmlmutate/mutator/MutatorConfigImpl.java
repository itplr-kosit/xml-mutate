package de.kosit.xmlmutate.mutator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.kosit.xmlmutate.tester.Expectation;

/**
 * MutatorConfigImpl
 *
 * @author Renzo Kottmann
 */
public class MutatorConfigImpl implements MutatorConfig {

    private final static Logger log = LogManager.getLogger(MutatorConfigImpl.class);

    private boolean schemaValid = true;
    private Map<String, String> config = null;
    private ArrayList<Expectation> schematronExpectations = new ArrayList<Expectation>();

    MutatorConfigImpl() {
        config = new HashMap<String, String>();
    }

    public void addConfigItem(String name, String value) {
        config.put(name, value);
    }

    @Override
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
        if (name == null) {
            throw new IllegalStateException("No mutator name of xmute instruction given");
        }
        return name;
    }

    @Override
    public boolean expectSchemaValid() {
        return this.schemaValid;
    }

    public void setExpectSchemaValid(boolean valid) {
        this.schemaValid = valid;
    }

    public void addSchematronExpectation(Expectation expectation) {
        if (Objects.nonNull(expectation)) {
            this.schematronExpectations.add(expectation);
        }
    }

	@Override
	public List<Expectation> getSchematronExpectations() {
		return this.schematronExpectations;
	}

}