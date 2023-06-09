package de.kosit.xmlmutate.runner;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.TestResource.E2ESchematronWithoutAnyShadowedRules;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.mutator.IdentityMutator;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class E2eMutationEvaluationStatusesTest {

  /**
   * Expected from test scenario:
   * <li>ERROR - Mutator definition has errors</li>
   * <li>ERROR - Mutator references rule not defined in schematron</li>
   * <li>FAILURE - When mutation succeeds but rule assertion fails</li>
   */
  @Test
  void shouldDistinctBetweenFailuresAndErrors() {
    final URI testXmlResource = E2ESchematronWithoutAnyShadowedRules.XML_WITH_INVALID_MUTATOR;
    List<Schematron> schematronWithShadowedRules = getSchematronXslRules();
    final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
        schematronWithShadowedRules, FailureMode.FAIL_AT_END);
    final MutationRunner runner = new MutationRunner(runnerConfig,
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

    RunnerResult result = runner.run();

    List<Pair<Path, List<Mutation>>> resultList = result.getResult();

    assertThat(resultList).isNotNull().hasSize(1);
    assertThat(resultList.get(0)).isNotNull();
    assertThat(resultList.get(0).getRight()).isNotNull().hasSize(3);
    Map<String, Mutation> mutatorRuleReferences = resultList.get(0).getRight().stream()
        .collect(toMap(mutation -> mutation.getConfiguration().getMutationId(), mutation -> mutation));
    assertThat(mutatorRuleReferences).containsKeys(null, "magazineWithArticleCount-id-1", "anyWithCodeLength-id-1");


    Map<String, Mutation> mutations = resultList.get(0).getRight().stream()
        .collect(toMap(mutation -> mutation.getConfiguration().getMutationId(), mutation -> mutation));
    assertThat(mutations).containsKeys(null, "magazineWithArticleCount-id-1", "anyWithCodeLength-id-1");

    // state is ERROR - mutator definition invalid. Validation may not be evaluated.
    assertThat(mutations.get(null).getMutator()).isNull();
    assertThat(mutations.get(null).getState()).isEqualTo(State.ERROR);
    assertThat(mutations.get(null).getResult()).isNotNull();
    assertThat(mutations.get(null).getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.UNPROCESSED);
    assertThat(mutations.get(null).getResult().getSchematronResult()).isEmpty();
    assertThat(mutations.get(null).getMutationErrorContainer()).isNotNull();
    assertThat(mutations.get(null).getMutationErrorContainer()
        .getGlobalErrorMessages()).isNotNull().isNotEmpty();


    // state is ERROR - mutator is referencing rule that is not defined in schematron.
    assertThat(mutations.get("magazineWithArticleCount-id-1").getMutator())
        .isNotNull().isInstanceOf(IdentityMutator.class);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getState()).isEqualTo(State.ERROR);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult()).isNotNull();
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.INVALID);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult().getSchematronResult()).isNotEmpty().hasSize(1);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getMutationErrorContainer()).isNotNull();
    assertThat(mutations.get("magazineWithArticleCount-id-1").getMutationErrorContainer()
        .getGlobalErrorMessages()).isNotNull().isEmpty();
    assertThat(mutations.get("magazineWithArticleCount-id-1").getMutationErrorContainer()
        .getAllErrorMessages()).isNotNull().isNotEmpty().hasSize(1);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getMutationErrorContainer()
        .getAllErrorMessages().get(0)).isEqualTo("Rule magazineWithArticleCountNonExixtant does not exist in schematron efde");


    // valida mutator definition but rule assertion fails when validating xml
    assertThat(mutations.get("anyWithCodeLength-id-1").getMutator())
        .isNotNull().isInstanceOf(IdentityMutator.class);
    assertThat(mutations.get("anyWithCodeLength-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult()).isNotNull();
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult()
        .getSchematronValidationState()).isEqualTo(ValidationState.INVALID);
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult()
        .getSchematronResult()).isNotEmpty();

    assertThat(mutations.get("anyWithCodeLength-id-1").getResult()
        .getSchematronExpectationMatches()).isNotEmpty().hasSize(1);
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult()
        .getSchematronExpectationMatches().keySet().stream().findFirst().map(
            SchematronRuleExpectation::getRuleName).orElse(StringUtils.EMPTY)).isNotEmpty().isEqualTo("anyWithCodeLength");
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult()
        .getSchematronExpectationMatches().values().stream().findFirst().orElseThrow()).isFalse();

    assertThat(mutations.get("anyWithCodeLength-id-1").getMutationErrorContainer()).isNotNull();
    assertThat(mutations.get("anyWithCodeLength-id-1").getMutationErrorContainer()
        .getAllErrorMessages()).isNotNull().isNotEmpty().hasSize(1);
    assertThat(mutations.get("anyWithCodeLength-id-1").getMutationErrorContainer()
        .getAllErrorMessages().get(0)).isNotNull().isEqualTo("Failed rule anyWithCodeLength instruction anyWithCodeLength-id-1");
  }

  @Test
  void shouldNotBeAnyShadowedRuleEvaluations() {
    final URI testXmlResource = E2ESchematronWithoutAnyShadowedRules.XML_SHADOWING_SOLVED;
    List<Schematron> schematronWithShadowedRules = getSchematronXslRules();
    final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
        schematronWithShadowedRules, FailureMode.FAIL_AT_END);
    final MutationRunner runner = new MutationRunner(runnerConfig,
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

    RunnerResult result = runner.run();

    List<Pair<Path, List<Mutation>>> resultList = result.getResult();

    assertThat(resultList).isNotNull().hasSize(1);
    assertThat(resultList.get(0)).isNotNull();
    assertThat(resultList.get(0).getValue()).isNotNull().isNotEmpty().hasSize(3);

    Map<String, Mutation> mutations = resultList.get(0).getRight().stream()
        .collect(toMap(mutation -> mutation.getConfiguration().getMutationId(), mutation -> mutation));
    assertThat(mutations).containsKeys("bookWithPageCount-id-1", "magazineWithArticleCount-id-1",
        "anyWithCodeLength-id-1");
    assertThat(mutations.get("bookWithPageCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("bookWithPageCount-id-1").getResult()).isNotNull();
    assertThat(mutations.get("bookWithPageCount-id-1").getResult().getSchematronValidationState()).isEqualTo(ValidationState.VALID);

    assertThat(mutations.get("magazineWithArticleCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult()).isNotNull();
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult().getSchematronValidationState()).isEqualTo(ValidationState.VALID);

    assertThat(mutations.get("anyWithCodeLength-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult()).isNotNull();
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult().getSchematronValidationState()).isEqualTo(ValidationState.INVALID);

  }

  private List<Schematron> getSchematronXslRules() {
    final List<Schematron> schematronList = new ArrayList<>();
    final URI uri = E2ESchematronWithoutAnyShadowedRules.XSL_SCHEMATRON;
    final List<String> list = Arrays.asList("bookWithPageCount", "magazineWithArticleCount", "anyWithCodeLength");
    final Schematron schematron = new Schematron("efde", uri, list);
    schematronList.add(schematron);
    return schematronList;
  }

}
