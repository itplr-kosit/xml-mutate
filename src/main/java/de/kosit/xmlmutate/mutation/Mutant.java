package de.kosit.xmlmutate.mutation;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.w3c.dom.DocumentFragment;

import lombok.Getter;

import de.kosit.xmlmutate.mutator.Mutator;
import de.kosit.xmlmutate.parser.MutatorInstruction;
import de.kosit.xmlmutate.runner.MutationState;

/**
 *
 * @author Renzo Kottmann
 */

public class Mutant {

    private MutationState state = MutationState.CREATED;
    private DocumentFragment mutant = null;
    /**
     * The MutatorInstruction which is the basis of this Mutant.
     */
    private MutatorInstruction instruction = null;

    public Mutant(DocumentFragment mutant) {
        this.mutant = mutant;
    }
}
