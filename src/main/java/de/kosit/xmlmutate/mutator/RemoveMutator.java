package de.kosit.xmlmutate.mutator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import lombok.extern.slf4j.Slf4j;
import de.kosit.xmlmutate.cli.XmlMutateUtil;
import de.kosit.xmlmutate.mutation.Mutant;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.parser.MutatorInstruction;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Removes the target element or attribute (Element by default) and exchanges it
 * with a XML comment
 *
 * @author Andreas Penski
 * @author Renzo Kottmann
 */

public class RemoveMutator extends BaseMutator {
    private static final Logger log = LoggerFactory.getLogger(RemoveMutator.class);
    /**
     * The name of the accepted MutatorInstruction Property.
     */
    public final String ATTRIBUTE_PROPERTY_NAME = "attribute";

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public List<Mutant> mutate(MutatorInstruction instruction) {
        final ArrayList<Mutant> mutants = new ArrayList<>();
        DocumentFragment clone = instruction.getClone();

        List<String> attr_names = instruction.getProperty(ATTRIBUTE_PROPERTY_NAME);
        if (attr_names != null && !attr_names.isEmpty()) {

            removeAttributes(clone, attr_names);

        } else {
            removeElement(clone);
        }

        mutants.add(new Mutant(clone, instruction));

        return mutants;
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {

        throw new NoSuchMethodError();
    }

    // could be just the child element. consider this
    private void removeAttributes(final DocumentFragment clone, final List<String> attributeNames) {
        log.trace("Clone={} and attr={}", XmlMutateUtil.printToString(clone, 2), attributeNames);
        if (clone == null) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Clone is null");
        }

        if (!clone.hasChildNodes()) {
            log.error("clone has no childs, nothing to remove.");
        }

        attributeNames.forEach(a -> {
            log.trace("removing attr={}", a);
            // final Node attr = nodeMap.getNamedItem(a.toString());
            final Element elem = (Element) clone.getFirstChild();
            if (elem.hasAttribute(a)) {
                elem.removeAttribute(a);
            }
        });
    }

    private void removeElement(DocumentFragment clone) {
        final Node child = clone.getFirstChild();
        log.trace("Removing from clone={} first child={}", XmlMutateUtil.printToString(clone), child);
        // even shorter way would be to just create an empty DocuemtnFragement
        // but just to be explicit, all content of the clone gets deleted

        clone.removeChild(child);
    }

}
