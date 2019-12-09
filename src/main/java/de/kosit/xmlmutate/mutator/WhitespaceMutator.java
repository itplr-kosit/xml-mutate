package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.ErrorCode;
import de.kosit.xmlmutate.runner.MutationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    private static final String WHITESPACE = " ";

    // platform independent line break (for java would be \r\n)
    private static final String CARRIAGE_RETURN = System.lineSeparator();

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


    @RequiredArgsConstructor
    public enum Variation {
        WHITESPACE_PREFIX(1),
        WHITESPACE_SUFFIX(2),
        WHITESPACE_REPLACEMENT(3),
        CARRIAGE_RETURN_PREFIX(4),
        CARRIAGE_RETURN_SUFFIX(5),
        CARRIAGE_RETURN_REPLACEMENT(6);

        @Getter
        private final int order;

        public static Variation getVariationWithOrder(final int order) {
            for (Variation e : Variation.values()) {
                if (order == e.order) return e;
            }
            throw new MutationException(de.kosit.xmlmutate.mutator.ErrorCode.WHITESPACE_VARIATION_UNKNOWN, order);
        }


        public String getNewTextContent(final String contextToChange) {
            switch (this) {
                case WHITESPACE_PREFIX:
                    // Size is total length of final string
                    return StringUtils.leftPad(contextToChange, contextToChange.length() + 1, WHITESPACE);
                case WHITESPACE_SUFFIX:
                    return StringUtils.rightPad(contextToChange, contextToChange.length() + 1, WHITESPACE);
                case WHITESPACE_REPLACEMENT:
                    return WHITESPACE;
                case CARRIAGE_RETURN_PREFIX:
                    return CARRIAGE_RETURN + contextToChange;
                case CARRIAGE_RETURN_SUFFIX:
                    return contextToChange + CARRIAGE_RETURN;
                case CARRIAGE_RETURN_REPLACEMENT:
                    return CARRIAGE_RETURN;
                default:
                    throw new IllegalArgumentException("Whitespace mutator variation unknown");
            }
        }

    }
}
