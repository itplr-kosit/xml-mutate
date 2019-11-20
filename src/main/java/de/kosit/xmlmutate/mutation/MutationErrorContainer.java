package de.kosit.xmlmutate.mutation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class MutationErrorContainer {

    // Map because each schematron rule name will have a respective error message
    private Map<String, String> schematronErrorMessages = new HashMap<>();

    private List<String> schemaErrorMessages = new ArrayList<>();

    private List<String> globalErrorMessages = new ArrayList<>();

    private List<String> allErrorMessages = new ArrayList<>();


    public void addSchematronErrorMessage(final String ruleName, final String message) {
        this.getSchematronErrorMessages().put(ruleName, message);
    }

    public void addSchemaErrorMessage(final String message) {
        this.getSchemaErrorMessages().add(message);
    }

    public void addGlobalErrorMessage(final String message) {
        this.getGlobalErrorMessages().add(message);
    }

    /**
     * To create a common error message list for the report generator in the order needed
     *
     * @param schematronRuleNamesFailed - the list of the schematron rules that failed
     */
    public void createCommonErrorMessageList(final List<String> schematronRuleNamesFailed) {
        for (final String ruleNameFailed : schematronRuleNamesFailed) {
            this.allErrorMessages.add(this.schematronErrorMessages.get(ruleNameFailed + ":N"));
        }
        this.allErrorMessages.addAll(this.globalErrorMessages);
        this.allErrorMessages.addAll(this.schemaErrorMessages);
    }

}
