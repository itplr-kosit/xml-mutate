package de.kosit.xmlmutate.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.util.List;
import java.util.Optional;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import com.google.common.base.Optional;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationResult;
import de.kosit.xmlmutate.mutation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation.State;

/**
 * Prüft die definierten Assertions bei den Schematron-Regeln gegebenüber dem
 * validierten Zustand.
 *
 * @author Andreas Penski
 */

public class EvaluateSchematronExpectationsAction implements RunAction {
    private static final Logger log = LoggerFactory.getLogger(EvaluateSchematronExpectationsAction.class);

    @Override
    public void run(final Mutation mutation) {

        mutation.getConfiguration().getSchematronExpectations().forEach(e -> {

            final boolean valid = this.evaluate(e, mutation.getResult());

            mutation.getResult().getSchematronExpectationMatches().put(e, valid);
            // Todo if this is needed
            if (!valid) {
                mutation.setErrorMessage("Failed expectation assert for " + e.getRuleName());
            }
            log.trace(
                    "mutator={} rule={} mustPass={} mustFail={} evaluatedValid={}", mutation.getMutator().getName(),
                    e.getRuleName(), e.mustPass(), e.mustFail(), valid);
        });
        mutation.setState(State.CHECKED);
    }

    // Evalutes if result matches expectation
    private boolean evaluate(final SchematronRuleExpectation expectation, MutationResult result) {
        final Collection<SchematronOutput> targets;
        if (expectation.getSource() != null) {
            final Optional<SchematronOutput> schematronResult = result.getSchematronResult(expectation.getSource());

            targets = schematronResult.map(Collections::singletonList).orElseGet(ArrayList::new);
        } else {
            targets = result.getSchematronResult().values();
        }
        // get all failed assertions of schematron which matches the given expection
        final Optional<FailedAssert> failed = targets.stream().map(SchematronOutput::getFailedAsserts)
                .flatMap(List::stream).peek(f -> {
                    // log.debug("FA id={} flag={}", f.getId(), f.getFlag());
                }).filter(f -> f.getId().equals(expectation.getRuleName())).findFirst();
        // log.debug("Evaluation for {} ", failed.)
        boolean failedAsExpected = failed.isPresent() && expectation.mustFail();
        boolean noFailedButExpected = !failed.isPresent() && expectation.mustPass();
        log.trace("failedAsExpected={} or  noFailedButExpected={}", failedAsExpected, noFailedButExpected);
        return failedAsExpected || noFailedButExpected;
    }

    // xmute mutator = "noop" schematron-invalid="UBL-CR-001"

    // xmute mutator = "remove" schematron-valid="UBL-CR-001"
}