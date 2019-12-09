package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author Victor del Campo
 */
@Slf4j
public class WhitespaceMutator  extends BaseMutator {

    final static String MUTATOR_NAME = "whitespace";

    static final String INTERNAL_PROP_OPTION = WhitespaceMutator.class.getSimpleName() + ".whitespace";

    @Override
    public List<String> getNames() {
        return Collections.singletonList(MUTATOR_NAME);
    }

    @Override
    public void mutate(final MutationContext context, final MutationConfig config) {
        final Node target = context.getTarget();
        if (streamElements(target.getChildNodes()).count() > 0) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Found complex content, but was expecting a single text value");
        }

        if (StringUtils.isBlank(target.getTextContent())) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Element content is empty");
        }
        target.setTextContent(config.getProperties().get(INTERNAL_PROP_OPTION).toString());
    }
}
