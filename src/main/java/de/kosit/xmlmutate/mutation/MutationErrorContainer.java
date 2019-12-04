package de.kosit.xmlmutate.mutation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MutationErrorContainer {

    // Map because each schematron rule name will have a respective error message
    private Map<String, Exception> schematronErrorMessages = new HashMap<>();

    private List<Exception> schemaErrorMessages = new ArrayList<>();

    // Getter needed for tests
    @Getter
    private List<Exception> globalErrorMessages = new ArrayList<>();


    public void addSchematronErrorMessage(final String ruleName, final Exception exception) {
        this.schematronErrorMessages.put(ruleName, exception);
    }

    public void addSchemaErrorMessage(final Exception exception) {

        this.schemaErrorMessages.add(exception);
    }

    public void addGlobalErrorMessage(final Exception exception) {
        this.globalErrorMessages.add(exception);
    }

    /**
     * To create a common error message list for the report generator in the order needed
     *
     * @param schematronRuleNamesFailed - the list of the schematron rules that failed
     */
    public List<String> getAllErrorMessagesSorted(final List<String> schematronRuleNamesFailed) {
        final List<String> allErrorsList = new ArrayList<>();
        for (final String ruleNameFailed : schematronRuleNamesFailed) {
            allErrorsList.add(this.schematronErrorMessages.get(ruleNameFailed).getMessage());
        }
        allErrorsList.addAll(this.globalErrorMessages.stream().map(Throwable::getMessage).collect(Collectors.toList()));
        allErrorsList.addAll(this.schemaErrorMessages.stream().map(Throwable::getMessage).collect(Collectors.toList()));
        return allErrorsList;
    }

    /**
     * To know if the mutation has any kind of error messages
     *
     * @return true or false
     */
    public boolean hasAnyErrors() {
        return !globalErrorMessages.isEmpty() || !schemaErrorMessages.isEmpty() || !schematronErrorMessages.isEmpty();
    }

}
