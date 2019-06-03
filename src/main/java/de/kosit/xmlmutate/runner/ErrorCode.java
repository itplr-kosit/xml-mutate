package de.kosit.xmlmutate.runner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.init.kosit.commons.util.NamedError;

/**
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public enum ErrorCode implements NamedError {

    RESET_NULL("Error resetting node to original state: parent or target is null"),

    WRONG_OWNER("Target Node is not in the correct document. Can not reset original state"),

    WRONG_PARENT("There is structural error. Parents are not the same, but this is expected"),

    STRUCTURAL_MISMATCH("Structural mismatch: {0}"), CONFIGURATION_ERRROR("Configuration error: {0}");

    @Getter
    private final String template;
}
