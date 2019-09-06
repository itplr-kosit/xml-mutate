package de.kosit.xmlmutate.runner;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.parser.MutatorInstruction;

/**
 * All context information about processing all MutatorInstruction of a single
 * docuemnt.
 */
public class RunnerDocumentContext {

    private final List<MutatorInstruction> instruction;
    private final Document originalDocument;

    public RunnerDocumentContext(Document doc, List<MutatorInstruction> instruction) {
        this.originalDocument = doc;
        this.instruction = instruction;

    }

    public List<MutatorInstruction> getInstructions() {
        return this.instruction;
    }

    Document getOriginalDocument() {
        return this.originalDocument;
    }
}
