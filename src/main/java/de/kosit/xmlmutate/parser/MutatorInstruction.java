package de.kosit.xmlmutate.parser;

import java.util.List;
import java.util.Properties;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.Mutant;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutator.Mutator;

import de.kosit.xmlmutate.runner.MutatorDocumentContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MutatorInstruction {

    final private Properties props = new Properties();
    private boolean schemaExpectation = true;
    private MutatorDocumentContext documentContext;
    private boolean hasError = false;
    private Mutator mutator = null;

    void addProperty(String key, String value) {
        this.props.setProperty(key, value);
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    public void setSchemaValidationAsExpected(boolean valid) {
        this.schemaExpectation = valid;
    }

    public void setDocumentContext(MutatorDocumentContext context) {
        this.documentContext = context;
    }

    public int getLevel() {
        return this.documentContext.getLevel();
    }

    public boolean hasError() {
        return this.hasError;
    }

    public Element getTarget() {
        return this.documentContext.getTarget();
    }

    public List<Mutant> execute() {
        return mutator.mutate(this);
    }

    public String getMutatorName() {
        return mutator != null ? this.mutator.getName() : "undefined";
    }

    public DocumentFragment getClone() {
        return this.documentContext.getCloneFragment();
    }

    // TODO: catch null
    void createMutator(String name) {
        log.trace("createMutator with name={}", name);

        this.mutator = MutatorPool.getMutator(name);
    }
}
