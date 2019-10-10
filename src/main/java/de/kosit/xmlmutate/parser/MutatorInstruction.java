package de.kosit.xmlmutate.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.kosit.xmlmutate.mutation.Mutant;
import de.kosit.xmlmutate.mutator.Mutator;

import de.kosit.xmlmutate.runner.MutatorDocumentContext;

public class MutatorInstruction {

    private static final Logger log = LoggerFactory.getLogger(MutatorInstruction.class);

    final private Map<String, List<String>> props = new HashMap<String, List<String>>();
    private boolean schemaExpectation = true;
    private MutatorDocumentContext documentContext;
    private boolean hasError = false;
    private Mutator mutator = null;

    void addProperty(String key, List<String> value) {
        if (key == null) {
            throw new IllegalArgumentException("A key must exist and not be null");
        }
        if (value == null) {
            value = new ArrayList<String>();
        }
        this.props.put(key, value);
    }

    public List<String> getProperty(String name) {
        if (props.containsKey(name)) {
            return this.props.get(name);
        }
        return new ArrayList<String>();
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

    public List<Mutant> createMutants() {
        return mutator.mutate(this);
    }

    public String getMutatorName() {
        return mutator != null ? this.mutator.getName() : "undefined";
    }

    public DocumentFragment getOriginalFragment() {
        return this.documentContext.getOriginalFragment();
    }

    public DocumentFragment getClone() {
        return this.documentContext.getCloneFragment();
    }

    public ProcessingInstruction getPI() {
        return this.documentContext.getPi();
    }

    // TODO: catch null
    void createMutator(String name) {
        log.trace("createMutator with name={}", name);

        this.mutator = MutatorPool.getMutator(name);
    }
}
