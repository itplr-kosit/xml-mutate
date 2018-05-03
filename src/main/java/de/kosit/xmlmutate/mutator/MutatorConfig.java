package de.kosit.xmlmutate.mutator;

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
}