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

    private static final String MUTATOR_NAME = "whitespace";

    private static final String PROP_LIST = "list";

    private static final String PROP_POSITION = "position";

    private static final String PROP_LENGTH = "length";

    private static final String SEPARATOR = ",";

    private static final int DEFAULT_LENGTH = 5;

    private static final Position DEFAULT_POSITION = Position.REPLACE;

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
        final String content = createMutatedContent(context.getTarget().getTextContent(), config);
        target.setTextContent(content);
    }

    private String createMutatedContent(final String contentToChange, final MutationConfig config) {
        final Position position = determinePosition(config);
        final int length = determineLength(config);
        final List<XmlWhitespaceCharacter> charactersToUse = determineCharacters(config);
        final String whiteSpaceSequence = createWhitespaceSequence(length, charactersToUse);
        return getNewTextContent(position, contentToChange, whiteSpaceSequence);
    }


    private Position determinePosition(MutationConfig config) {
        Position position = DEFAULT_POSITION;
        if (config.getProperties().get(PROP_POSITION) != null) {
            preCheckProp(config, PROP_POSITION);
            position = Position.fromString(config.resolveList(PROP_POSITION).get(0).toString().toUpperCase());
        }
        return position;
    }

    private int determineLength(MutationConfig config) {
        int length = DEFAULT_LENGTH;
        if (config.getProperties().get(PROP_LENGTH) != null) {
            preCheckProp(config, PROP_LENGTH);
            final String number = config.resolveList(PROP_LENGTH).get(0).toString();
            try {
                length = Integer.parseInt(number);
            } catch (final NumberFormatException e) {
                throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Length parameter value is not an integer");
            }
        }
        return length;
    }

    private List<XmlWhitespaceCharacter> determineCharacters(MutationConfig config) {
        final List<XmlWhitespaceCharacter> charactersToUse = new ArrayList<>();
        if (config.getProperties().get(PROP_LIST) != null) {
            charactersToUse.addAll(config.resolveList(PROP_LIST).stream().flatMap(e -> Arrays.stream(e.toString().split(SEPARATOR)))
                    .filter(StringUtils::isNotEmpty)
                    .map(String::trim)
                    .map(t -> XmlWhitespaceCharacter.fromString(t.toUpperCase())).collect(Collectors.toList()));
        }
        if (charactersToUse.isEmpty()) {
            charactersToUse.addAll(Arrays.asList(XmlWhitespaceCharacter.values()));
        }
        return charactersToUse;
    }

    private void preCheckProp(final MutationConfig config, final String property) {
        // length and position parameter can only be declared once and have only one value
        final List<Object> objects = config.resolveList(property);
        if (objects.size() > 1) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Only 1 " + property.toLowerCase() + " parameter declaration allowed");
        } else if (objects.toString().split(SEPARATOR).length > 1) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Only 1 " + property.toLowerCase() + " parameter value allowed");
        }
    }

    private String getNewTextContent(final Position position, final String contentToChange, final String whiteSpaceSequence) {
        switch (position) {
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


    private String createWhitespaceSequence(final int length, final List<XmlWhitespaceCharacter> charactersToUse) {
        final StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sequence.append(charactersToUse.get(new Random().nextInt(charactersToUse.size())).getValue());
        }
        return sequence.toString();
    }

    @RequiredArgsConstructor
    public enum XmlWhitespaceCharacter {
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
        public static XmlWhitespaceCharacter fromString(final String text) {
            try {
                return XmlWhitespaceCharacter.valueOf(text);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Whitespace mutator list character unknown: " + text.toLowerCase());
            }
        }
    }


    public enum Position {
        PREFIX,
        SUFFIX,
        REPLACE;

        /**
         * To get the specific enum variation from a string
         *
         * @param text - the string name of the enum
         * @return the enum
         */
        public static Position fromString(final String text) {
            try {
                return Position.valueOf(text);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Whitespace mutator position unknown: " + text.toLowerCase());
            }
        }

    }

}
