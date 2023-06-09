package de.kosit.xmlmutate.runner;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import de.kosit.xmlmutate.TestHelper;
import de.kosit.xmlmutate.TestResource.E2ESchematronWithShadowedRules;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutation.Schematron;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class E2eMutationShadowedRulesTest {

  private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  @Test
  void shouldPassTheHappyPathScenarioAllValidSchematronRules() {
    final URI testXmlResource = E2ESchematronWithShadowedRules.XML_HAPPY_PATH;
    List<Schematron> schematronWithShadowedRules = getSchematronXslWithShadowedRulesHappyPathScenario();
    final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
        schematronWithShadowedRules, FailureMode.FAIL_AT_END);
    final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

    RunnerResult result = runner.run();

    List<Pair<Path, List<Mutation>>> resultList = result.getResult();

    assertThat(resultList).isNotNull().hasSize(1);
    Map<String, Mutation> mutations = resultList.get(0).getRight().stream()
        .collect(toMap(mutation -> mutation.getConfiguration().getMutationId(), mutation -> mutation));
    assertThat(mutations).containsKeys("bookWithPageCount-id-1", "magazineWithArticleCount-id-1");
    assertThat(mutations.get("bookWithPageCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("bookWithPageCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
  }

  @Test
  @DisplayName("Mutator has expectation of rule 'anyWithCodeLength' evaluated as invalid. "
      + "But actually 'anyWithCodeLength' is not executed by schematron engine because of shadowed "
      + "rule xpath. The mutator expectation for rule 'anyWithCodeLength' is marked as failed.")
  void shouldFailOneSchematronRule() {
    final URI testXmlResource = E2ESchematronWithShadowedRules.XML_MAGAZINE_FAILURE_SHADOWED;
    List<Schematron> schematrons = getSchematronXslWithShadowedRules();
    final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
        schematrons, FailureMode.FAIL_AT_END);
    final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

    RunnerResult result = runner.run();

    List<Pair<Path, List<Mutation>>> resultList = result.getResult();

    assertThat(resultList).isNotNull().hasSize(1);

    Map<String, Mutation> mutations = resultList.get(0).getRight().stream()
        .collect(toMap(mutation -> mutation.getConfiguration().getMutationId(), mutation -> mutation));
    assertThat(mutations).containsKeys("magazineWithArticleCount-id-1", "anyWithCodeLength-id-1",
        "bookWithPageCount-id-1");
    assertThat(mutations.get("magazineWithArticleCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
    assertThat(mutations.get("bookWithPageCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("bookWithPageCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
    // the 'anyWithCodeLength-id-1' is treated in a wrong way because of rules shadowing in shematron!
    assertThat(mutations.get("anyWithCodeLength-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
  }

  @Test
  @DisplayName("Rule 'anyWithCodeLength' is expected to pass by mutator 'anyWithCodeLength-id-1'."
      + "But according to the schematron rule 'anyWithCodeLength' - actually it fails because 'code'"
      + "length is not == 4. Even SVRL report doesn't contain the error because rule 'anyWithCodeLength'"
      + "xpath has been shadowed. So we have hidden error in XML that is not caught by mutator.")
  void shouldFailOneSchematronRuleButActuallyPasses() {
    final URI testXmlResource = E2ESchematronWithShadowedRules.XML_MAGAZINE_FAILURE_SHADOWED_INVALID;
    List<Schematron> schematrons = getSchematronXslWithShadowedRules();
    final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
        schematrons, FailureMode.FAIL_AT_END);
    final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

    RunnerResult result = runner.run();

    List<Pair<Path, List<Mutation>>> resultList = result.getResult();

    assertThat(resultList).isNotNull().hasSize(1);

    Map<String, Mutation> mutations = resultList.get(0).getRight().stream()
        .collect(toMap(mutation -> mutation.getConfiguration().getMutationId(), mutation -> mutation));
    assertThat(mutations).containsKeys("bookWithPageCount-id-1", "magazineWithArticleCount-id-1",
        "anyWithCodeLength-id-1");

    assertThat(mutations.get("bookWithPageCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("bookWithPageCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
    // the 'anyWithCodeLength-id-1' is treated in a wrong way!
    // actual error lost because of invalid schematron definition having shadowed rules
    assertThat(mutations.get("anyWithCodeLength-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("anyWithCodeLength-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
  }

  @Test
  @DisplayName("According to specific mutator 'bookWithPageCount-id-1' it should pass but"
      + "XML contains one more item under the rule XPATH which actually fails but doesn't have"
      + "any mutator attached to that specific failing element.")
  void shouldFailOneSchematronRuleButActuallyPasses1() {
    final URI testXmlResource = E2ESchematronWithShadowedRules.XML_MULTIPLE_XPATHS_ONE_FAILS;
    List<Schematron> schematrons = getSchematronXslWithShadowedRules();
    final RunnerConfig runnerConfig = TestHelper.createSchematronRunnerConfig(testXmlResource,
        schematrons, FailureMode.FAIL_AT_END);
    final MutationRunner runner = new MutationRunner(runnerConfig, this.executor);

    RunnerResult result = runner.run();

    List<Pair<Path, List<Mutation>>> resultList = result.getResult();

    assertThat(resultList).isNotNull().hasSize(1);
    Map<String, Mutation> mutations = resultList.get(0).getRight().stream()
        .collect(toMap(mutation -> mutation.getConfiguration().getMutationId(), mutation -> mutation));
    assertThat(mutations).containsKeys("magazineWithArticleCount-id-1", "bookWithPageCount-id-1");

    assertThat(mutations.get("magazineWithArticleCount-id-1").getState()).isEqualTo(State.CHECKED);
    assertThat(mutations.get("magazineWithArticleCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.VALID);
    assertThat(mutations.get("bookWithPageCount-id-1").getState()).isEqualTo(State.CHECKED);
    // actually the element right after the mutator is passing!
    assertThat(mutations.get("bookWithPageCount-id-1").getResult().getSchematronValidationState()).isEqualTo(
        ValidationState.INVALID);
  }

  private List<Schematron> getSchematronXslWithShadowedRulesHappyPathScenario() {
    final List<Schematron> schematronList = new ArrayList<>();
    final URI uri = E2ESchematronWithShadowedRules.XSL_SCHEMATRON;
    final List<String> list = Arrays.asList("bookWithPageCount", "magazineWithArticleCount");
    final Schematron schematron = new Schematron("efde", uri, list);
    schematronList.add(schematron);
    return schematronList;
  }

  private List<Schematron> getSchematronXslWithShadowedRules() {
    final List<Schematron> schematronList = new ArrayList<>();
    final URI uri = E2ESchematronWithShadowedRules.XSL_SCHEMATRON;
    final List<String> list = Arrays.asList("bookWithPageCount", "magazineWithArticleCount", "anyWithCodeLength");
    final Schematron schematron = new Schematron("efde", uri, list);
    schematronList.add(schematron);
    return schematronList;
  }

}
