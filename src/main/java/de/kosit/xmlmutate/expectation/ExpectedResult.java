package de.kosit.xmlmutate.expectation;

import de.kosit.xmlmutate.mutation.MutationResult;

public enum ExpectedResult {
    FAIL, PASS;

    public boolean meetsValidationState(final MutationResult.ValidationState validationState) {
        return (this.equals(ExpectedResult.PASS) && validationState.equals(MutationResult.ValidationState.VALID))
                || (this.equals(ExpectedResult.FAIL) && (validationState.equals(MutationResult.ValidationState.INVALID)
                || validationState.equals(MutationResult.ValidationState.UNPROCESSED)));
    }
}
