package de.kosit.xmlmutate.mutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

public class MutationErrorContainer {

    // Map because each schematron rule name will have a respective error message
    private final Map<String, Exception> schematronErrorMessages = new HashMap<>();

    private final List<Exception> schemaErrorMessages = new ArrayList<>();

    // Getter needed for tests
    @Getter
    private final List<Exception> globalErrorMessages = new ArrayList<>();


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
     * To create a common error message list for the report generator in the order needed
     *
     */
    public List<String> getAllErrorMessages() {
        final List<String> allErrorsList = new ArrayList<>();
        allErrorsList.addAll(this.schematronErrorMessages.values().stream().map(Exception::getMessage).collect(Collectors.toList()));
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
        return !this.globalErrorMessages.isEmpty() || !this.schemaErrorMessages.isEmpty() || !this.schematronErrorMessages.isEmpty();
    }

}
