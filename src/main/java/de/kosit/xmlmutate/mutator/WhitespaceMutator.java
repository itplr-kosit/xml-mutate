package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Victor del Campo
 */
@Slf4j
public class WhitespaceMutator extends BaseMutator {

    static final String NAME = "whitespace";

    static final String INTERNAL_PROP_VALUE = WhitespaceMutator.class.getSimpleName() + ".whitespace";

    @Override
    public List<String> getNames() {
        return Collections.singletonList(NAME);
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
        target.setTextContent(config.getProperties().get(INTERNAL_PROP_VALUE).toString());
    }


    enum Position {

        PREFIX {
            // Size is total length of final string
            @Override
            public String randomAddCharacters(final String contentToChange, final String whiteSpaceSequence) {
                return StringUtils.leftPad(contentToChange, contentToChange.length() + whiteSpaceSequence.length(), whiteSpaceSequence);
            }
        },
        SUFFIX {
            @Override
            public String randomAddCharacters(final String contentToChange, final String whiteSpaceSequence) {
                return StringUtils.rightPad(contentToChange, contentToChange.length() + whiteSpaceSequence.length(), whiteSpaceSequence);
            }
        },
        REPLACE {
            @Override
            public String randomAddCharacters(final String contentToChange, final String whiteSpaceSequence) {
                return whiteSpaceSequence;
            }
        },
        MIX {
            @Override
            public String randomAddCharacters(final String contentToChange, final String whiteSpaceSequence) {
                return null;
            }
        };

        /**
         * To get the specific enum variation from a string
         *
         * @param text - the string name of the enum
         * @return the enum
         */
        static Position fromString(final String text) {
            try {
                return Position.valueOf(text);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Whitespace mutator position unknown: " + text.toLowerCase());
            }
        }

        /**
         * To get the functional enum of Position
         *
         * @return the prefix, suffix and replace enum
         */
        static List<Position> getAllPositionsButMix() {
            return Arrays.asList(PREFIX, SUFFIX, REPLACE);
        }


        public abstract String randomAddCharacters(final String contentToChange, final String whiteSpaceSequence);


    }


}
