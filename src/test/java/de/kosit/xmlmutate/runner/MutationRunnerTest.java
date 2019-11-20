package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.mutation.Mutation;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Victor del Campo
 */
public class MutationRunnerTest {

    final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Test
    @DisplayName("Test with same id declared for different PI")
    public void testPIidsNotUnique() {
        final RunnerConfig runnerConfig = TestHelper.createRunnerConfig("src/test/resources/book/book_duplicate_ids.xml");
        final MutationRunner runner = new MutationRunner(runnerConfig, executor);
        final List<Mutation> mutations = runner.parseMutations(DocumentParser.readDocument(runnerConfig.getDocuments().get(0)), "book_duplicate_ids.xml");
        assertThat(mutations).hasSize(2);
        assertThat(mutations.get(0).getState()).isEqualTo(Mutation.State.CREATED);
        assertThat(mutations.get(0).getMutationErrorContainer().getGlobalErrorMessages().size()).isEqualTo(0);
        // #46 "Tough question. I would say it should NOT lead to an error mutation"
        assertThat(mutations.get(1).getState()).isEqualTo(Mutation.State.CREATED);
        assertThat(mutations.get(1).getMutationErrorContainer().getGlobalErrorMessages().size()).isGreaterThanOrEqualTo(1);
        assertThat(mutations.get(1).getMutationErrorContainer().getGlobalErrorMessages().stream().anyMatch(e -> StringUtils.containsIgnoreCase(e,"Mutation instruction id was already declared"))).isTrue();
    }

}
