package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.*;
import java.util.stream.Collectors;

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


    @RequiredArgsConstructor
    enum XmlWhitespaceCharacter {
        // Also CR is \n, since the Transformer in the serialization is transforming \r to the HEX value &#2D
        CR("\n"),
        LF("\n"),
        CRLF("\n"),
        TAB("\t"),
        SPACE(" ");

        @Getter
        private final String value;

        /**
         * To get the specific enum variation from a string
         *
         * @param text - the string name of the enum
         * @return the enum
         */
        static XmlWhitespaceCharacter fromString(final String text) {
            try {
                return XmlWhitespaceCharacter.valueOf(text);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Whitespace mutator list character unknown: " + text.toLowerCase());
            }
        }
    }

    enum Position {
        PREFIX,
        SUFFIX,
        REPLACE,
        MIX;

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

        /**
         * Creating new text content adding (or replacing) with xml whitespace characters. Each mutation with have a different whitespace sequence
         *
         * @param contentToChange - the old text content
         * @param charactersToUse - the list of xml whitespace characters that should be used
         * @param length          - the number of xml whitespace characters to use
         * @return the new text content
         */
        String getNewTextContent(final String contentToChange, final List<XmlWhitespaceCharacter> charactersToUse, final int length) {
            final String whiteSpaceSequence = createWhitespaceSequence(length, charactersToUse);
            switch (this) {
                case PREFIX:
                    // Size is total length of final string
                    return StringUtils.leftPad(contentToChange, contentToChange.length() + whiteSpaceSequence.length(), whiteSpaceSequence);
                case SUFFIX:
                    return StringUtils.rightPad(contentToChange, contentToChange.length() + whiteSpaceSequence.length(), whiteSpaceSequence);
                case REPLACE:
                    return whiteSpaceSequence;
                default:
                    throw new IllegalArgumentException("Whitespace mutator variation unknown");
            }
        }

    }

    static String createWhitespaceSequence(final int length, final List<XmlWhitespaceCharacter> charactersToUse) {
        final StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sequence.append(charactersToUse.get(new Random().nextInt(charactersToUse.size())).getValue());
        }
        return sequence.toString();
    }


}
