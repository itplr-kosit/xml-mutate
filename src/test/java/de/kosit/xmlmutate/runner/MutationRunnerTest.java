package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.mutation.Mutation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @author Victor del Campo
 */
public class MutationRunnerTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final static String PATH_TO_BOOK_FOLDER = "src/test/resources/book/";

    @Test
    @DisplayName("Test with same id declared for different PI")
    public void testPIidsNotUnique() {
        final String documentName = "book_duplicate_ids.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName);
        assertThat(mutations).hasSize(2);
        assertThat(mutations.get(0).getState()).isEqualTo(Mutation.State.CREATED);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isEqualTo(0);
        // #46 "Tough question. I would say it should NOT lead to an error mutation"
        assertThat(mutations.get(1).getState()).isEqualTo(Mutation.State.CREATED);
        assertThat(mutations.get(1).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(1).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(e -> StringUtils.containsIgnoreCase(e.getMessage(), "Mutation instruction id was already declared"))).isTrue();
    }

    @Test
    @DisplayName("Test with fail fast failure mode and a parsing error in the 1st mutation of total of 2")
    public void testFailFastParserError1stMutation() {
        final String documentName = "book_parsing_error_1st_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName);
        assertThat(mutations).hasSize(1);
    }

    @Test
    @DisplayName("Test with fail fast failure mode and a parsing error in the 2nd mutation of total of 2")
    public void testFailFastParserError2ndMutation() {
        final String documentName = "book_parsing_error_2nd_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName);
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail at end failure mode and a parsing error in the 1st mutation of total of 2")
    public void testFailAtEndParserError1stMutation() {
        final String documentName = "book_parsing_error_1st_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName);
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail at end failure mode and a parsing error in the 2nd mutation of total of 2")
    public void testFailAtEndParserError2ndMutation() {
        final String documentName = "book_parsing_error_2nd_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName);
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail never failure mode and a parsing error in the 1st mutation of total of 2")
    public void testFailNeverParserError1stMutation() {
        final String documentName = "book_parsing_error_1st_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_NEVER);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName);
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail never failure mode and a parsing error in the 2nd mutation of total of 2")
    public void testFailNeverParserError2ndMutation() {
        final String documentName = "book_parsing_error_2nd_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_NEVER);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName);
        assertThat(mutations).hasSize(2);
    }

    @Test
    @DisplayName("Test with fail fast failure mode and an action error in the 1st mutation of total of 2")
    public void testFailFastActionError1stMutation() {
        final String documentName = "book_action_error_1st_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);

        Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(PATH_TO_BOOK_FOLDER + documentName));
        try {
            assertThat(resultFuture.get().getValue()).hasSize(1);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test with fail fast failure mode and an action error in the 2nd mutation of total of 2")
    public void testFailFastActionError2ndMutation() {
        final String documentName = "book_action_error_2nd_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_FAST);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);

        Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(PATH_TO_BOOK_FOLDER + documentName));
        try {
            assertThat(resultFuture.get().getValue()).hasSize(2);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test with fail at end failure mode and an action error in the 1st mutation of total of 2")
    public void testFailAtEndActionError1stMutation() {
        final String documentName = "book_action_error_1st_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_AT_END);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);

        Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(PATH_TO_BOOK_FOLDER + documentName));
        try {
            assertThat(resultFuture.get().getValue()).hasSize(2);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test with fail never failure mode and an action error in the 1st mutation of total of 2")
    public void testFailNeverActionError1stMutation() {
        final String documentName = "book_action_error_1st_mutation.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_NEVER);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);

        Future<Pair<Path, List<Mutation>>> resultFuture = runner.process(Paths.get(PATH_TO_BOOK_FOLDER + documentName));
        try {
            assertThat(resultFuture.get().getValue()).hasSize(2);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    @Test
    @DisplayName("Test with an schema invalid original document that should not be ignored")
    public void testOriginalXmlNotValidNotIgnore() {
        final String documentName = "book_original_invalid_schema.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        assertThrows(MutationException.class, runner::run, "Original document " + documentName + " is not schema valid");
    }

    @Test
    @DisplayName("Test with an schema invalid original document that should be ignored")
    public void testOriginalXmlNotValidIgnore() {
        final String documentName = "book_original_invalid_schema.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, true);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        assertThatCode(runner::run).doesNotThrowAnyException();
    }


    @Test
    @DisplayName("Test without a schema in CLI but with an expectation of schema in a PI")
    public void testSchemaInPInotInCLI() {
        final String documentName = "book.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_NEVER);
        runnerConfig.setSchema(null);
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        assertThrows(MutationException.class,  () ->
            runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName),
                    "No schema given at CLI but expectation declared in a xmute pi");
    }

    @Test
    @DisplayName("Test without a schematron in CLI but with an expectation of schematron in a PI")
    public void testSchematronInPInotInCLI() {
        final String documentName = "book.xml";
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig(PATH_TO_BOOK_FOLDER + documentName, FailureMode.FAIL_NEVER);
        runnerConfig.setSchematronRules(Collections.emptyList());
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        assertThrows(MutationException.class,  () ->
                        runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), documentName),
                "No schematron given at CLI but expectation declared in a xmute pi");
    }
}
