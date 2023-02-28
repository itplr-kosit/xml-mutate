package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.runner.MutationException;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Node;

/**
 * Mutator for codes. It produces a simple text replacement of the code values. The actual configuration of
 * {@link de.kosit.xmlmutate.mutation.Mutation} is carried out through the {@link CodeMutationGenerator}.
 *
 * @author Andreas Penski
 */
public class CodeMutator extends BaseMutator {

    static final String NAME = "code";

    static final String INTERNAL_PROP_VALUE = CodeMutator.class.getSimpleName() + ".code";

    static final String PROP_ATTRIBUTE = "attribute";

    @Override
    public List<String> getNames() {
        return Collections.singletonList(NAME);
    }

    @Override
    public void mutate(final MutationDocumentContext context, final MutationConfig config) {
        final Node target = resolveTarget(context, config);
        if (streamElements(target.getChildNodes()).findAny().isPresent()) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Found complex content, but was expecting a single text value");
        }
        target.setTextContent(config.getProperties().get(INTERNAL_PROP_VALUE).toString());
    }

    private Node resolveTarget(final MutationDocumentContext context, final MutationConfig config) {
        final Node target;
        final Object attr = config.getProperties().get(PROP_ATTRIBUTE);
        if (attr != null) {
            target = context.getTarget().getAttributes().getNamedItem(attr.toString());
            if (target == null) {
                throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, String.format("No attribute named %s found", attr));
            }

        } else {
            target = context.getTarget();
        }
        return target;
    }

}
