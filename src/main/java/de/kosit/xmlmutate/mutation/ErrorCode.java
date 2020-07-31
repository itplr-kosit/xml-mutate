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

    NO_MUTATOR_FOUND("No valid mutator found for {0}"),

    NO_MUTATION_FOUND("No mutation can be found for {0}. Is PI last element?"),

    SCHEMA_ERROR("Schema validation error: {0}"),

    SCHEMATRON_RULE_DEFINITION_ERROR("Schematron rule definition incorrect: {0}");

    @Getter
    private final String template;
}
