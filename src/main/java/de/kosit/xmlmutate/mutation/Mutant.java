package de.kosit.xmlmutate.mutation;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.kosit.xmlmutate.parser.MutatorInstruction;
import de.kosit.xmlmutate.runner.MutationState;

/**
 *
 * @author Renzo Kottmann
 */

public class Mutant {

    private MutationState state = MutationState.CREATED;
    private DocumentFragment mutant = null;
    private String name = "undefined";

    /**
     * The MutatorInstruction which is the basis of this Mutant.
     */
    private MutatorInstruction instruction = null;

    public Mutant(DocumentFragment mutant, MutatorInstruction instruction) {
        this.mutant = mutant;
        this.instruction = instruction;
    }

    public String getName() {
        return this.name;
    }

    public DocumentFragment getOriginalFragment() {
        return this.instruction.getOriginalFragment();
    }

    public DocumentFragment getCloneFragment() {
        return this.instruction.getClone();
    }

    public DocumentFragment getMutatedFragment() {
        return this.mutant;
    }

    public ProcessingInstruction getPI() {
        return this.instruction.getPI();
    }

    public Element getTarget() {
        return instruction.getTarget();
    }
}
