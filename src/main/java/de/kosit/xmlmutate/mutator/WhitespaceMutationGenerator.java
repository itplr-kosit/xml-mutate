package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationGenerator;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Generator for whitespace mutation having three parameters (position, list and length)
 *
 * @author Victor del Campo
 */
@Slf4j
public class WhitespaceMutationGenerator implements MutationGenerator {

    private static final String PROP_LIST = "list";

    private static final String PROP_POSITION = "position";

    private static final String PROP_LENGTH = "length";

    private static final int DEFAULT_LENGTH = 5;

    private static final String SEPARATOR = ",";


    @Override
    public List<String> getNames() {
        return Collections.singletonList(WhitespaceMutator.NAME);
    }

    @Override
    public String getPreferredName() {
        return getNames().stream().findFirst().orElseThrow(IllegalStateException::new);
    }

    @Override
    public List<Mutation> generateMutations(MutationConfig config, MutationContext context) {

        final List<WhitespaceMutator.Position> positions = determinePositions(config);
        final int length = determineLength(config);
        final List<WhitespaceMutator.XmlWhitespaceCharacter> charactersToUse = determineWhitespaceCharacters(config);

        final List<Mutation> list = positions.stream().map(e ->
                createMutation(config, context, e, e.getNewTextContent(context.getTarget().getTextContent(), charactersToUse, length)))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            throw new MutationException(ErrorCode.CONFIGURATION_ERRROR, "No code source found");
        }
        return list;
    }


    private List<WhitespaceMutator.Position> determinePositions(MutationConfig config) {
        final List<WhitespaceMutator.Position> positions = new ArrayList<>();
        if (config.getProperties().get(PROP_POSITION) != null) {
            positions.addAll(config.resolveList(PROP_POSITION).stream().flatMap(e -> Arrays.stream(e.toString().split(SEPARATOR)))
                    .filter(StringUtils::isNotEmpty)
                    .map(String::trim)
                    .distinct()
                    .map(t -> WhitespaceMutator.Position.fromString(t.toUpperCase())).collect(Collectors.toList()));
        }
        if (positions.isEmpty() || positions.contains(WhitespaceMutator.Position.MIX)) {
            positions.clear();
            positions.addAll(WhitespaceMutator.Position.getAllPositionsButMix());
        }
        return positions;
    }

    private int determineLength(MutationConfig config) {
        int length = DEFAULT_LENGTH;
        if (config.getProperties().get(PROP_LENGTH) != null) {
            preCheckLengthParam(config);
            final String number = config.resolveList(PROP_LENGTH).get(0).toString();
            try {
                length = Integer.parseInt(number);
            } catch (final NumberFormatException e) {
                throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Length parameter value is not an integer");
            }
        }
        return length;
    }


    private void preCheckLengthParam(final MutationConfig config) {
        final List<Object> objects = config.resolveList(PROP_LENGTH);
        if (objects.size() > 1) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Only 1 length parameter declaration allowed");
        } else if (objects.toString().split(SEPARATOR).length > 1) {
            throw new MutationException(ErrorCode.STRUCTURAL_MISMATCH, "Only 1 length parameter value allowed");
        }
    }

    private List<WhitespaceMutator.XmlWhitespaceCharacter> determineWhitespaceCharacters(MutationConfig config) {
        final List<WhitespaceMutator.XmlWhitespaceCharacter> charactersToUse = new ArrayList<>();
        if (config.getProperties().get(PROP_LIST) != null) {
            charactersToUse.addAll(config.resolveList(PROP_LIST).stream().flatMap(e -> Arrays.stream(e.toString().split(SEPARATOR)))
                    .filter(StringUtils::isNotEmpty)
                    .map(String::trim)
                    .distinct()
                    .map(t -> WhitespaceMutator.XmlWhitespaceCharacter.fromString(t.toUpperCase())).collect(Collectors.toList()));
        }
        if (charactersToUse.isEmpty()) {
            charactersToUse.addAll(Arrays.asList(WhitespaceMutator.XmlWhitespaceCharacter.values()));
        }
        return charactersToUse;
    }

    private Mutation createMutation(final MutationConfig config, final MutationContext context, final WhitespaceMutator.Position position, final String s) {
        final Mutator mutator = MutatorRegistry.getInstance().getMutator(getPreferredName());
        final MutationConfig cloned = config.cloneConfig();
        cloned.add(WhitespaceMutator.INTERNAL_PROP_VALUE, s);
        return new Mutation(context.cloneContext(),
                Services.getNameGenerator().generateName(context.getDocumentName(), position.name()), cloned, mutator);
    }


}
