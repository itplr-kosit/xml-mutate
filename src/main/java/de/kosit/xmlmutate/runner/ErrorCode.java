package de.kosit.xmlmutate.runner;

/**
 * @author Renzo Kottmann
 * @author Andreas Penski
 */

public enum ErrorCode {

    RESET_NULL("Error resetting node to original state: parent or target is null: {0}"),

    WRONG_OWNER("Target Node is not in the correct document. Can not reset original state: {0}"),

    WRONG_PARENT("There is structural error. Parents are not the same, but this is expected: {0}"),

    STRUCTURAL_MISMATCH("Structural mismatch: {0}"),

    CONFIGURATION_ERRROR("Configuration error: {0}"),

    TRANSFORM_ERROR("Error while transforming: {0}"),

    GENERAL_ERROR("Error: {0}");

    private final String template;

    ErrorCode(String code) {
        this.template = code;
    }

    public String message() {
        return this.message("");
    }

    public String message(String text) {
        return String.format(this.template, text);
    }

}
