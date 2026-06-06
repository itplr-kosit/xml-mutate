package de.kosit.xmlmutate.expectation;

import de.kosit.xmlmutate.mutation.MutationResult;

/**
 * Enum for expectation result
 *
 * @author Victor del Campo
 */
public enum ExpectedResult {
    FAIL, PASS, UNDEFINED;

    /**
     * Given a validation state, it evaluates if it corresponds to the expectation result
     *
     * @param validationState - the given validation state
     * @return true or false
     */
    public boolean meetsValidationState(final MutationResult.ValidationState validationState) {
        return (this.equals(ExpectedResult.PASS) && validationState.equals(MutationResult.ValidationState.VALID))
                || (this.equals(ExpectedResult.FAIL) && (validationState.equals(MutationResult.ValidationState.INVALID)
                || validationState.equals(MutationResult.ValidationState.UNPROCESSED)));
    }
}
