package de.kosit.xmlmutate.mutation;

import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.Getter;
import lombok.Setter;

import de.kosit.xmlmutate.mutator.Mutator;

/**
 * Sammelobjekt für eine Mutation innerhalb einer Test-Datei. Sammelt alle möglichen Informationen rund um die
 * Verarbeitung, inkl. Testergebnisse.
 *
 * @author Andreas Penski
 */

@Getter
@Setter
public class Mutation {

    private final MutationContext context;

    private String identifier;

    private MutationConfig configuration = new MutationConfig();

    private Mutator mutator;

    private MutationResult result = new MutationResult();

    private State state = State.CREATED;

    private String errorMessage;

    /**
     * Constructor.
     * 
     * @param context der {@link MutationContext}
     * @param identifier ein Name / Bezeichner für die Mutation
     */
    public Mutation(final MutationContext context, final String identifier) {
        this.context = context;
        this.identifier = identifier;
    }

    public Path getResultDocument() {
        return Paths.get(getContext().getDocumentName()).resolve(this.identifier + ".xml");
    }

    public boolean isInvalid() {
        return !isValid();
    }

    public boolean isValid() {
        return this.state.ordinal() >= State.VALIDATED.ordinal() && this.result.isValid();
    }

    public boolean isErroneous() {
        return this.state == State.ERROR;
    }

    public enum State {
        ERROR, CREATED, MUTATED, VALIDATED, CHECKED;
    }

}
