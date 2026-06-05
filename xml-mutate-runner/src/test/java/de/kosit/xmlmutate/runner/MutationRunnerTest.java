package de.kosit.xmlmutate.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.TestResource;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ProcessingInstruction;

/**
 * @author Victor del Campo
 */
public class MutationRunnerTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Test
    @DisplayName("Test with same id declared for different PI")
    public void testPIidsNotUnique() {
        final URI uri = TestResource.BookResources.DUPLICATE_IDS;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)),
                Paths.get(uri));
        assertThat(mutations).hasSize(2);
        assertThat(mutations.get(0).getState()).isEqualTo(Mutation.State.CREATED);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages()).isEmpty();
        // #46 "Tough question. I would say it should NOT lead to an error mutation"
        assertThat(mutations.get(1).getState()).isEqualTo(Mutation.State.CREATED);
        assertThat(mutations.get(1).getMutationErrorContainer().getGlobalErrorMessages()).isNotEmpty();
        assertThat(mutations.get(1).getMutationErrorContainer().getGlobalErrorMessages().stream()
                .anyMatch(e -> StringUtils.containsIgnoreCase(e.getMessage(), "Mutation instruction id was already declared"))).isTrue();
    }

    @Test
    @DisplayName("Test with fail fast failure mode and a parsing error in the 1st mutation of total of 2")
    public void testFailFastParserError1stMutation() {
        final URI uri = TestResource.BookResources.PARSER_ERROR_1ST_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)),
                Paths.get(uri));
        assertThat(mutations).hasSize(1);
    }

    @Test
    @DisplayName("Test with fail fast failure mode and a parsing error in the 2nd mutation of total of 2")
    public void testFailFastParserError2ndMutation() {
        final URI uri = TestResource.BookResources.PARSER_ERROR_2ND_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)),
                Paths.get(uri));
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail at end failure mode and a parsing error in the 1st mutation of total of 2")
    public void testFailAtEndParserError1stMutation() {
        final URI uri = TestResource.BookResources.PARSER_ERROR_1ST_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)),
                Paths.get(uri));
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail at end failure mode and a parsing error in the 2nd mutation of total of 2")
    public void testFailAtEndParserError2ndMutation() {
        final URI uri = TestResource.BookResources.PARSER_ERROR_2ND_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)),
                Paths.get(uri));
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail never failure mode and a parsing error in the 1st mutation of total of 2")
    public void testFailNeverParserError1stMutation() {
        final URI uri = TestResource.BookResources.PARSER_ERROR_1ST_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_NEVER);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)),
                Paths.get(uri));
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail never failure mode and a parsing error in the 2nd mutation of total of 2")
    public void testFailNeverParserError2ndMutation() {
        final URI uri = TestResource.BookResources.PARSER_ERROR_2ND_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_NEVER);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)),
                Paths.get(uri));
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail fast failure mode and an action error in the 1st mutation of total of 2")
    public void testFailFastActionError1stMutation() {
        final URI uri = TestResource.BookResources.ACTION_ERROR_1ST_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        final Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(uri));
        try {
            assertThat(resultFuture.get().getValue()).hasSize(1);
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test with fail fast failure mode and an action error with the 2nd failed mutation of total of 2")
    public void testFailFastActionError2ndMutation() {
        final URI uri = TestResource.BookResources.ACTION_ERROR_2ND_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        final Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(uri));
        try {
            assertThat(resultFuture.get().getValue()).hasSize(2);
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should fail fast right with the very first invalid mutator definition skipping all subsequent mutators")
    public void testFailFastActionErrorWith3rdErroneousMutator() {
        final URI uri = TestResource.BookResources.ACTION_ERROR_3RD_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        final Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(uri));
        try {
            List<Mutation> result = resultFuture.get().getValue();
            assertThat(result.stream()
                .map(Mutation::getContext)
                .map(MutationDocumentContext::getPi)
                .map(ProcessingInstruction::getData))
                .isNotEmpty()
                .hasSize(3)
                .containsExactly(
                    "mutator=\"remove\" schema-valid schematron-invalid=\"schematron:Book-1\" id=\"id1\"",
                    "mutator=\"identity\" schema-valid id=\"id11\"",
                    "mutator=\"remove\" shema-valid shematron-valid=\"schematron:Book-2\" id=\"id1\""
                );
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("Should parse all mutators including erroneous and running all only valid mutators")
    public void testFailAtEndActionErrorWith3rdErroneousMutator() {
        final URI uri = TestResource.BookResources.ACTION_ERROR_3RD_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        final Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(uri));
        try {
            List<Mutation> result = resultFuture.get().getValue();
            assertThat(result.stream()
                .map(Mutation::getContext)
                .map(MutationDocumentContext::getPi)
                .map(ProcessingInstruction::getData))
                .isNotEmpty()
                .hasSize(4)
                .containsExactly(
                    "mutator=\"remove\" schema-valid schematron-invalid=\"schematron:Book-1\" id=\"id1\"",
                    "mutator=\"identity\" schema-valid id=\"id11\"",
                    "mutator=\"remove\" shema-valid shematron-valid=\"schematron:Book-2\" id=\"id1\"",
                    "mutator=\"identity\" schema-valid id=\"id12\""
                );
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("Test with fail at end failure mode and an action error in the 1st mutation of total of 2")
    public void testFailAtEndActionError1stMutation() {

        final URI uri = TestResource.BookResources.ACTION_ERROR_1ST_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        final Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(uri));
        try {
            assertThat(resultFuture.get().getValue()).hasSize(2);
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test with fail never failure mode and an action error in the 1st mutation of total of 2")
    public void testFailNeverActionError1stMutation() {
        final URI uri = TestResource.BookResources.ACTION_ERROR_1ST_MUTATION;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_NEVER);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

        final Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(uri));
        try {
            assertThat(resultFuture.get().getValue()).hasSize(2);
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test with an schema invalid original document that should not be ignored")
    public void testOriginalXmlNotValidNotIgnore() {
        final String documentName = "book_original_invalid_schema.xml";
        final URI uri = TestResource.BookResources.ORIGINAL_SCHEMA_INVALID;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        assertThrows(MutationException.class, runner::run, "Original document " + documentName + " is not schema valid");
    }

    @Test
    @DisplayName("Test with an schema invalid original document that should be ignored")
    public void testOriginalXmlNotValidIgnore() {
        final URI uri = TestResource.BookResources.ORIGINAL_SCHEMA_INVALID;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, true);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        assertThatCode(runner::run).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Test without a schema in CLI but with an expectation of schema in a PI")
    public void testSchemaInPInotInCLI() {
        final URI uri = TestResource.BookResources.SIMPLE;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_NEVER);
        runnerConfig.setSchema(null);
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        assertThrows(MutationException.class,
                () -> runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), Paths.get(uri)),
                "No schema given at CLI but expectation declared in a xmute pi");
    }

    @Test
    @DisplayName("Test without a schematron in CLI but with an expectation of schematron in a PI")
    public void testSchematronInPInotInCLI() {
        final URI uri = TestResource.BookResources.SIMPLE;
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(uri, FailureMode.FAIL_NEVER);
        runnerConfig.setSchematronRules(Collections.emptyList());
        final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);
        assertThrows(MutationException.class,
                () -> runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), Paths.get(uri)),
                "No schematron given at CLI but expectation declared in a xmute pi");
    }
}
