package de.kosit.xmlmutate.mutation;

public enum ExpectedResult {
    FAIL, PASS, UNDEFINED;

    public boolean meetsValidationState(final MutationResult.ValidationState validationState) {
        return (this.equals(ExpectedResult.PASS) && validationState.equals(MutationResult.ValidationState.VALID))
                || (this.equals(ExpectedResult.FAIL) && (validationState.equals(MutationResult.ValidationState.INVALID)
                || validationState.equals(MutationResult.ValidationState.UNPROCESSED)));
    }
}
