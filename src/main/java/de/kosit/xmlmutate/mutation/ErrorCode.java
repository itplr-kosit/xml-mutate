package de.kosit.xmlmutate.mutation;

import de.init.kosit.commons.util.NamedError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Victor del Campo
 */
@RequiredArgsConstructor
public enum ErrorCode implements NamedError {

    ID_CONTENT_EMPTY("Mutation instruction id can not be empty"),

    TAG_CONTENT_EMPTY("Mutation instruction tag can not be empty"),

    MORE_THAN_ONE_ID("Mutation instruction can only have 1 id"),

    ID_ALREADY_DECLARED("Mutation instruction id was already declared");

    @Getter
    private final String template;
}
