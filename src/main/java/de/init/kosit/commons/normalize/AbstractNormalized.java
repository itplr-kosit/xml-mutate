// Generated by delombok at Fri Aug 13 16:11:22 CEST 2021
package de.init.kosit.commons.normalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import de.init.kosit.commons.artefact.Artefact;

/**
 * Basis-Klasse für die eingelesenen Objekte.
 *
 * @author Andreas Penski
 */
public abstract class AbstractNormalized {
    private final List<Artefact> references = new ArrayList<>();

    /**
     * lifert alle Artefacts(main Artefact und alle Referenzen), die zu diesem Model gehören.
     *
     * @return alle Artefacts, die zu dem Modell gehören.
     */
    public Collection<Artefact> getArtefacts() {
        final List<Artefact> a = new ArrayList<>();
        a.addAll(this.references);
        return a;
    }

    /**
     * Fügt eine Referenz zum Model hinzu.
     *
     * @param reference
     */
    public final void addReference(@Nonnull final Artefact reference) {
       this.references.add(reference);
    }

    /**
     * Fügt ein Artefact zum Model hinzu.
     *
     * @param artefact
     */
    public void addArtefact(@Nonnull final Artefact artefact) {
        addReference(artefact);
    }

    public final Collection<Artefact> getReferences() {
        return this.references;
    }
}
