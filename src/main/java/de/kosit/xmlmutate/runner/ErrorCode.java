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

    INVALID_MUTATION_PRODUCED("Invalid xml mutation produced"),

    MUTATION_XML_FILE_READ_PROBLEM("Error while while trying to read the xml mutation file"),

    SCHEMATRON_RULE_NOT_EXIST("Rule {0} does not exist in schematron {1}"),

    SCHEMATRON_RULE_FAILED_EXPECTATION("Failed expectation"),

    SCHEMATRON_RULE_FAILED_EXPECTATION_ENTERITY("Failed expectation"),

    ACTION_RUNNER_ERROR("Error running action {0} in mutation {1} "),

    ID_ALREADY_DECLARED("Mutation instruction id was already declared"),

    SCHEMATRON_EVALUATION_ERROR("Schematron evaluation error: {0}"),

    ORIGINAL_XML_NOT_SCHEMA_VALID("Original document {0} is not schema valid:{1}"),

    CLI_ARGUMENT_NOT_PRESENT_BUT_PI_EXPECTATION("No {0} given at CLI but expectation declared in a xmute pi");


    @Getter
    private final String template;
}
