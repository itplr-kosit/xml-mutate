package de.kosit.xmlmutate.mutator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.init.kosit.commons.util.NamedError;

/**
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public enum ErrorCode implements NamedError {

    TRANSFORM_ERROR("Error while transforming: {0}"),

    WHITESPACE_VARIATION_UNKNOWN("No whitespace mutator variation found with number: {0}");

    @Getter
    private final String template;
}
