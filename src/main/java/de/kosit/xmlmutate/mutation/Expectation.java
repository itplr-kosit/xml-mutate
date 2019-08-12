package de.kosit.xmlmutate.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Daten über das erwartete Schematron-Validierungsergebnis NACH der Mutation.
 */
@AllArgsConstructor
@Getter
public class Expectation {

    /**
     * Der (Kurz-)Name der Schematron-Quelle (falls bei der Validierung mehrere
     * Scripte verwendet werden)
     */
    private final String schematronSource;

    /**
     * Der Name der Regel die geprüft werden soll
     */
    private final String ruleName;

    /**
     *
     */
    private final ExpectedResult expectedResult;

    /**
     * An expectation on a subject e.g. name of a Schematron result, or XML Schema
     */
    public String schematron() {
        return this.schematronSource;
    };

    /**
     * Of() what do we expect something e.g. which Schematron rule do we expect to
     * be true/or false
     */
    public String ruleName() {
        return this.ruleName;
    };

    /**
     * Is expected to fail?
     */
    public boolean mustFail() {
        return ExpectedResult.FAIL.equals(this.expectedResult);
    }

    /**
     * Is the expected to pass?
     */
    public boolean mustPass() {
        return ExpectedResult.PASS.equals(this.expectedResult);
    }

    public boolean evaluate(final MutationResult result) {
        final Collection<SchematronOutput> targets;
        if (getSchematronSource() != null) {
            final Optional<SchematronOutput> schematronResult = result.getSchematronResult(getSchematronSource());
            targets = schematronResult.map(Collections::singletonList).orElseGet(ArrayList::new);
        } else {
            targets = result.getSchematronResult().values();
        }

        // TODO prüfen ob das auch bei keinem Schematron match stimmt
        final Optional<FailedAssert> failed = targets.stream().map(SchematronOutput::getFailedAsserts)
                .flatMap(List::stream).filter(f -> f.getId().equals(ruleName())).findAny();
        return (failed.isPresent() && mustFail()) || (!failed.isPresent() && mustPass());
    }

    public String evaluateMessage(final MutationResult result) {
        return null;
    }

    public enum ExpectedResult {
        FAIL, PASS
    }
}
