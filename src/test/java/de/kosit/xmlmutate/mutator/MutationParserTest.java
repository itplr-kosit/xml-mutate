package de.kosit.xmlmutate.mutator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import de.kosit.xmlmutate.mutation.MutationParser;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.ProcessingInstruction;

@ExtendWith(MockitoExtension.class)
class MutationParserTest {

  @Mock ProcessingInstruction pi;

  @ParameterizedTest
  @MethodSource("expectedOutcomeAndProvidedMutatorDefinitions")
  void shouldExtractCorrectIdentifierBasedOnProvidedMutators(final String expectedId, final String providedMutator) {
    when(pi.getData()).thenReturn(providedMutator);

    String result = MutationParser.extractProcessingInstructionId(pi);

    assertThat(result)
        .isNotNull()
        .isEqualTo(expectedId);
  }

  @DisplayName("When ID not provided return UNDEFINED")
  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {
      "   ",
      """
        mutator="identity"   schematron-valid = "efde:CR-DE-BT-510-UBO"\s
                             iddd    = "CR-DE-BT-510-UBO-id"   description ="Valid   BT-510"        
        """,
      """
          mutator="identity"   schematron-valid = "efde:CR-DE-BT-510-UBO"   description ="Valid   BT-510"   
          """}
  )
  void shouldNotFailExtractingProcessingInstructionIdentifierWhenIdentifierValueDoesNotExist(final String value) {
    when(pi.getData()).thenReturn(value);

    String result = MutationParser.extractProcessingInstructionId(pi);

    assertThat(result)
        .isNotNull()
        .isEqualTo("UNDEFINED");
  }


  @Test
  void shouldNotFailIfProcessingInstructionIsNotInstantiated() {
    String result = MutationParser.extractProcessingInstructionId(null);
    assertThat(result)
        .isNotNull()
        .isEqualTo("UNDEFINED");
  }

  private static Stream<Arguments> expectedOutcomeAndProvidedMutatorDefinitions() {
    return Stream.of(
        arguments("CR-DE-BT-510-UBO-remove",
            "mutator=\"remove\" schematron-invalid=\"efde:CR-DE-BT-510-UBO\" id=\"CR-DE-BT-510-UBO-remove\" description=\"BT-510 must exist\""),
        arguments("CR-DE-BT-510-UBO-id",
            """
                mutator="identity"   schematron-valid = "efde:CR-DE-BT-510-UBO"\s
                                    id= "CR-DE-BT-510-UBO-id"   description ="Valid   BT-510"        
                """),
        arguments("CR-DE-BT-510-UBO-id",
            """
                mutator="identity"   schematron-valid = "efde:CR-DE-BT-510-UBO"\s
                                     id    = " CR-DE-BT-510-UBO-id  "   description ="Valid   BT-510"        
                """),
        arguments("CR-DE-BT-510-UBO-remove",
            "mutator=\"remove\" schematron-invalid=\"efde:CR-DE-BT-510-UBO\" id=\"CR-DE-BT-510-UBO-remove\" id=\"expected_not_to_be_found\" description=\"BT-510 must exist\""
        ),
        arguments("CR-DE-BT-510-UBO-remove",
            "mutator=\"remove\" schematron-invalid=\"efde:CR-DE-BT-510-UBO id=\"qqq\" id=\"CR-DE-BT-510-UBO-remove\" id=\"expected_not_to_be_found\" description=\"BT-510 must exist\"")
    );
  }

}
