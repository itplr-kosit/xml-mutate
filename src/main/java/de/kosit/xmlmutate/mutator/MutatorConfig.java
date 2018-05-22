package de.kosit.xmlmutate.mutator;

import java.util.List;

import de.kosit.xmlmutate.tester.Expectation;

/**
 * package de.kosit.xmlmutate.mutator;
 *
 *
 * MutatorConfig

 */
public interface MutatorConfig {

    public void setInstructionName(String mutatorName);

    public String getInstructionName();

    public String getConfigItem(String name);

    public boolean expectSchemaValid();

    public List<Expectation> getSchematronExpectations();
}