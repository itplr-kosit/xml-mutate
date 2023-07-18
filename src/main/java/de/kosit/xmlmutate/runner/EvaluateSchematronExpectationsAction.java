package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronEnterity;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.mutation.MutationResult;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.schematron.SchematronCompiler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Check the assertions defined within the schematron rules againgst the validated state
 *
 * @author Andreas Penski
 */

public class EvaluateSchematronExpectationsAction implements RunAction {
    private static final Logger log = LoggerFactory.getLogger(EvaluateSchematronExpectationsAction.class);

    @Override
    public void run(final Mutation mutation) {
        final Pair<SchematronEnterity, ExpectedResult> schematronEnterityExpectation = mutation.getConfiguration().getSchematronEnterityExpectation();
        if (schematronEnterityExpectation == null) {
            for (final SchematronRuleExpectation expectation : mutation.getConfiguration().getSchematronExpectations()) {

                final boolean unknownRuleName = ifMutationHasRulesNotDefinedInSchematron(expectation, mutation.getResult().getSchematronResult());
                final String piId = StringUtils.isBlank(mutation.getConfiguration().getMutationId()) ?
                    StringUtils.EMPTY : mutation.getConfiguration().getMutationId();
                if (!unknownRuleName) {
                    final boolean valid = evaluate(expectation, mutation.getResult(),
                        mutation.getContext(), piId, unknownRuleName);

                    if (!valid) {
                        final MutationException processingException = createErrorMessage(
                            expectation.getRuleName(), piId);
                        mutation.getMutationErrorContainer().addSchematronErrorMessage(
                            expectation.getRuleName(), processingException);
                    }
                    log.trace(
                            "mutator={} rule={} mustPass={} mustFail={} evaluatedValid={}", mutation.getMutator().getNames(),
                            expectation.getRuleName(), expectation.expectValid(), expectation.expectInvalid(), valid);
                    mutation.getResult().getSchematronExpectationMatches().put(expectation, valid);
                } else {
                    mutation.setState(State.ERROR);
                    mutation.getMutationErrorContainer().addSchematronErrorMessage(expectation.getRuleName(), new MutationException(ErrorCode.SCHEMATRON_RULE_NOT_EXIST, expectation.getRuleName(), expectation.getSource() != null ? expectation.getSource() : "'no source'"));
                    log.debug("mutator={} has rule={} which is not defined in schematron", piId, expectation.getRuleName());
                    log.trace(
                            "mutator={} rule={} mustPass={} mustFail={} evaluatedValid={}", mutation.getMutator().getNames(),
                            expectation.getRuleName(), expectation.expectValid(), expectation.expectInvalid(), "unknown");
                    mutation.getResult().getSchematronExpectationMatches().put(expectation, false);
                }

            }
        } else {
            mutation.getResult().setSchematronExpectationMatches(evaluateSchematronEnterity(schematronEnterityExpectation, mutation));
        }
        if (!mutation.getState().equals(State.ERROR)) {
            mutation.setState(State.CHECKED);
        }

    }

    private MutationException createErrorMessage(final String ruleName, final String processingInstructionId) {
        if (StringUtils.isBlank(processingInstructionId)) {
            return new MutationException(ErrorCode.SCHEMATRON_RULE_FAILED_EXPECTATION, ruleName);
        }
        return new MutationException(
            ErrorCode.SCHEMATRON_RULE_WITH_INSTRUCTION_FAILED_EXPECTATION, ruleName, processingInstructionId);
    }

    private Map<SchematronRuleExpectation, Boolean> evaluateSchematronEnterity(final Pair<SchematronEnterity, ExpectedResult> schematronEnterityExpectation, final Mutation mutation) {
       final Map<SchematronRuleExpectation, Boolean> expectationsMatches = new HashMap<>();
       final ExpectedResult expectedResult = schematronEnterityExpectation.getValue();
        for (final Map.Entry<Schematron, SchematronOutput> entry : mutation.getResult().getSchematronResult().entrySet()) {
            final List<String> allRules = new SchematronCompiler().extractRulesIds(entry.getKey().getUri());
            final List<String> failedRules = entry.getValue().getFailedAsserts().stream()
                .map(FailedAssert::getId).toList();
            final List<String> passedRules = allRules.stream().filter(r -> !failedRules.contains(r)).toList();
            failedRules.forEach(f -> {
                if (expectationNotMet(schematronEnterityExpectation, true)) {
                    expectationsMatches.put(new SchematronRuleExpectation(entry.getKey().getName(), f, expectedResult), false);
                    mutation.setState(State.ERROR);
                    mutation.getMutationErrorContainer().addSchematronErrorMessage(f, new MutationException(ErrorCode.SCHEMATRON_RULE_FAILED_EXPECTATION_ENTERITY));

                }
            });
            passedRules.forEach(p -> {
                if (expectationNotMet(schematronEnterityExpectation, false)) {
                    expectationsMatches.put(new SchematronRuleExpectation(entry.getKey().getName(), p, expectedResult), false);
                    mutation.setState(State.ERROR);
                    mutation.getMutationErrorContainer().addSchematronErrorMessage(p, new MutationException(ErrorCode.SCHEMATRON_RULE_FAILED_EXPECTATION_ENTERITY));

                }
            });

        }
        return expectationsMatches;
    }

    private boolean expectationNotMet(final Pair<SchematronEnterity, ExpectedResult> schematronEnterityExpectation, final boolean failed) {
        final SchematronEnterity enterity = schematronEnterityExpectation.getKey();
        final ExpectedResult expected = schematronEnterityExpectation.getValue();
        boolean expectationMet = true;
        if ((enterity.equals(SchematronEnterity.ALL) && expected.equals(ExpectedResult.PASS) && failed)
                || (enterity.equals(SchematronEnterity.ALL) && expected.equals(ExpectedResult.FAIL) && !failed)
                || (enterity.equals(SchematronEnterity.NONE) && expected.equals(ExpectedResult.PASS) && !failed)
                || (enterity.equals(SchematronEnterity.NONE) && expected.equals(ExpectedResult.FAIL) && failed)) {
            expectationMet = false;
        }
        return !expectationMet;
    }

    private boolean ifMutationHasRulesNotDefinedInSchematron(SchematronRuleExpectation expectation,
        Map<Schematron, SchematronOutput> schematronResult) {
        return schematronResult.keySet().stream().anyMatch(schematron -> !schematron.hasRule(expectation));
    }

    // Evalutes if result matches expectation
    private boolean evaluate(final SchematronRuleExpectation expectation, MutationResult result,
        final MutationDocumentContext mutationDocumentContext, final String processingInstructionId,
        final boolean unknownRule) {
        final Collection<SchematronOutput> targets;
        if (expectation.getSource() != null) {
            final Optional<SchematronOutput> schematronResult = result.getSchematronResult(expectation.getSource());
            targets = schematronResult.map(Collections::singletonList).orElseGet(ArrayList::new);
        } else {
            targets = result.getSchematronResult().values();
        }

        doExplicitLogging(expectation, mutationDocumentContext, processingInstructionId, targets);

        final Optional<FailedAssert> failed = targets.stream().map(SchematronOutput::getFailedAsserts)
                .flatMap(List::stream).filter(f -> f.getId().equals(expectation.getRuleName())).findFirst();

        boolean failedAsExpected = failed.isPresent() && expectation.expectInvalid();
        boolean noFailedButExpected = failed.isEmpty() && expectation.expectValid() && !unknownRule;
        log.trace("failedAsExpected={} or  noFailedButExpected={}", failedAsExpected, noFailedButExpected);

        return failedAsExpected || noFailedButExpected;
    }

    private void doExplicitLogging(SchematronRuleExpectation expectation,
        MutationDocumentContext mutationDocumentContext, String processingInstructionId,
        Collection<SchematronOutput> targets) {
        final Map<String, Set<String>> baseXmlSchematronFailures = mutationDocumentContext.getSchematronFailures();
        final List<FailedAssert> actuallyFailed = filterOutFailuresFoundInOriginalXml(baseXmlSchematronFailures,
            targets);

        logIfExpectedSchematronRuleByMutatorHasAlreadyFailedWithoutAnyMutation(expectation.getRuleName(),
            mutationDocumentContext, processingInstructionId, baseXmlSchematronFailures);

        logUnexpectedSchematronErrorsByMutator(expectation, mutationDocumentContext,
            processingInstructionId, actuallyFailed);

        logIfMoreThenOneSchematronRuleFailedPerMutator(expectation, mutationDocumentContext,
            processingInstructionId, actuallyFailed);
    }

    /**
     * Logs if the same assertion fails multiple times in the document with mutation applied.
     *
     * @param expectation - mutator expectation on XML with schematron
     * @param mutationDocumentContext - all related details to testing XML
     * @param processingInstructionId - mutation identifier in the XML
     * @param actuallyFailed - i.e. rules failed without any mutations: "A", "B", "C" then
     *                       rules failed with mutation: "C", "D". Actually failed would be: "D"
     */
    private void logIfMoreThenOneSchematronRuleFailedPerMutator(SchematronRuleExpectation expectation,
        MutationDocumentContext mutationDocumentContext, String processingInstructionId,
        List<FailedAssert> actuallyFailed) {
        List<FailedAssert> failuresDefinedByMutator = findFailuresDefinedByMutator(actuallyFailed, expectation.getRuleName());
        if (failuresDefinedByMutator.size() > 1) {
            log.debug("{} -> Processing instruction {} has more then one schematron rule {} failed in svrl report. Picking very first one.",
                mutationDocumentContext.getDocumentName(), processingInstructionId, expectation.getRuleName());
        }
    }

    /**
     * Log any schematron failures that occured per mutator but was not expected by the mutator.
     *
     * @param expectation - mutator expectation on XML with schematron
     * @param mutationDocumentContext - all related details to testing XML
     * @param processingInstructionId - mutation identifier in the XML
     * @param actuallyFailed - mutation schematron failures without any errors that occured if
     *                       any on the XML without any mutations applied.
     *                       If rule A failed on XML without mutations and rules A, B and C
     *                       failed on mutator then result for actually failed is B and C.
     */
    private void logUnexpectedSchematronErrorsByMutator(SchematronRuleExpectation expectation,
        MutationDocumentContext mutationDocumentContext, String processingInstructionId,
        List<FailedAssert> actuallyFailed) {
        List<FailedAssert> failuresNotDefinedByMutator = findFailuresNotDefinedByMutator(
            actuallyFailed, expectation.getRuleName());
        if (!failuresNotDefinedByMutator.isEmpty()) {
            log.debug("{} -> Processing instruction {} has unexpected schematron assertion(-s) failures: {}",
                mutationDocumentContext.getDocumentName(), processingInstructionId,
                failuresNotDefinedByMutator.stream().map(FailedAssert::getId).toList());
        }
    }

    /**
     * Logs ERROR details if the same schematron rule has already failed on the XML without
     * any mutations applied on any XPATH.
     *
     * @param ruleName - identifies a schematron rule
     * @param mutationDocumentContext - all related details to testing XML
     * @param processingInstructionId - mutation identifier in the XML
     * @param baseXmlSchematronFailures - failures from SVRL on XML without any mutators applied
     */
    private void logIfExpectedSchematronRuleByMutatorHasAlreadyFailedWithoutAnyMutation(String ruleName,
        MutationDocumentContext mutationDocumentContext, String processingInstructionId,
        Map<String, Set<String>> baseXmlSchematronFailures) {
        if (baseXmlSchematronFailures.containsKey(ruleName)) {
            log.debug("{} -> Schematron rule {} has failed without any mutations. "
                    + "Processing instruction {} evaluation may be incorrect.",
                mutationDocumentContext.getDocumentName(), ruleName, processingInstructionId);
        }
    }

    private List<FailedAssert> findFailuresDefinedByMutator(List<FailedAssert> actuallyFailed, String ruleName) {
        return actuallyFailed.stream()
            .filter(failedAssert -> StringUtils.equals(failedAssert.getId(), ruleName))
            .toList();
    }

    /**
     * Return unexpected schematron failures.
     *
     * @param actuallyFailed schematron assertion failures from svrl with mutation applied
     *                       without any assertion failures from original XML document
     *                       schematron validation without any mutations applied.
     * @param ruleName schematron rule unique identifier from mutator definition
     *
     * @return unexpected failures that didn't happen while testing the original XML document
     * without mutators and were not expected by mutator.
     */
    private List<FailedAssert> findFailuresNotDefinedByMutator(List<FailedAssert> actuallyFailed, String ruleName) {
        return actuallyFailed.stream()
            .filter(failedAssert -> !StringUtils.equals(failedAssert.getId(), ruleName))
            .toList();
    }

    /**
     * Schematron assertion failures of mutated XML.
     * Includes the schematron failures per mutation which hasn't occurred in the XML
     * when validating the XML with schematron without any mutations. If there are schematron
     * errors with mutation applied same as without any mutations applied then such schematron
     * error is not included in the result.
     *
     * @param baseXmlSchematronFailures schematron failures from XML document without
     *                                  any mutations applied.
     *
     * @param targets schematron assertion failures on XML document with specific mutation applied.
     *
     * @return {@code targets} without {@code baseXmlSchematronFailures}
     */
    private List<FailedAssert> filterOutFailuresFoundInOriginalXml(Map<String, Set<String>> baseXmlSchematronFailures,
        Collection<SchematronOutput> targets) {
        return targets.stream()
            .map(SchematronOutput::getFailedAsserts)
            .flatMap(List::stream)
            .filter(failedAssert -> !baseXmlSchematronFailures.containsKey(failedAssert.getId()) ||
                baseXmlSchematronFailures.containsKey(failedAssert.getId()) &&
                    baseXmlSchematronFailures.get(failedAssert.getId()).stream()
                        .noneMatch(xpath -> StringUtils.equals(xpath, failedAssert.getLocation()))
            ).toList();
    }

}
