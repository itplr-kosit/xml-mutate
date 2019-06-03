package de.kosit.xmlmutate.mutator;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.tester.Expectation;

/**
 * AbstractMutator
 */
public abstract class AbstractMutator implements Mutator {

    MutatorConfig config = new MutatorConfigImpl();

    void addConfig(MutatorConfig config) {
        this.config = config;
    }

    @Override
    public MutatorConfig getConfig() {
        return config;
    }

    @Override
    public abstract Node execute(Element context);

    @Override
    public abstract String getName();

}