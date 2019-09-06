package de.kosit.xmlmutate.mutator;

import java.util.List;

import org.w3c.dom.Node;

import de.kosit.xmlmutate.mutation.Mutant;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.parser.MutatorInstruction;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;

/**
 * Mutator für Codes. Erzeugt eine einfache Textersetzung der Code-Werte. Die
 * eigentliche Konfiguration der {@link de.kosit.xmlmutate.mutation.Mutation}
 * erfolgt über den {@link CodeMutationGenerator}.
 *
 * @author Andreas Penski
 */
public class CodeMutator extends BaseMutator {

    static final String NAME = "code";

    static final String INTERNAL_PROP_VALUE = CodeMutator.class.getSimpleName() + ".code";

    static final String PROP_ATTRIBUTE = "attribute";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<Mutant> mutate(MutatorInstruction instruction) {
        throw new UnsupportedOperationException();
        // return null;
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
        final Node target = resolveTarget(context, config);
        if (streamElements(target.getChildNodes()).count() > 0) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH,
                    "Found complex content, but was expecting a single text value");
        }
        target.setTextContent(config.getProperties().get(INTERNAL_PROP_VALUE).toString());
    }

    private Node resolveTarget(final MutationContext context, final MutationConfig config) {
        final Node target;
        final Object attr = config.getProperties().get(PROP_ATTRIBUTE);
        if (attr != null) {
            target = context.getTarget().getAttributes().getNamedItem(attr.toString());
            if (target == null) {
                throw new MutationException(ErrorCode.CONFIGURATION_ERRROR,
                        String.format("No attribute named %s found", attr.toString()));
            }

        } else {
            target = context.getTarget();
        }
        return target;
    }
}
