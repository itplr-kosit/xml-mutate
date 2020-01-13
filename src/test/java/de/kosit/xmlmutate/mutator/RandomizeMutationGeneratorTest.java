package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.TestResource.TransformResource;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static de.kosit.xmlmutate.TestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the Generator for {@link RandomizeMutatorGenerator}.
 * 
 * @author Victor del Campo
 */
public class RandomizeMutationGeneratorTest {


    private RandomizeMutatorGenerator generator = new RandomizeMutatorGenerator();

    @Test
    @DisplayName("Test with more than 1 length parameter declared")
    public void testMaxParameterTooMany() {
        final MutationConfig config = createConfig().add("max", "2").add("max", "4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext()),
                ErrorCode.STRUCTURAL_MISMATCH + "Only 1 max parameter declaration allowed"
        );
    }

    @Test
    @DisplayName("Test with a non-integer max param value with a comma")
    public void testMaxParameterNoIntegerValueComma() {
        final MutationConfig config = createConfig().add("max", "2,4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext()),
                ErrorCode.STRUCTURAL_MISMATCH + "Only 1 integer max parameter value allowed"
        );
    }

    @Test
    @DisplayName("Test with a non-integer max param value with a dot")
    public void testMaxParameterNoIntegerValueDot() {
        final MutationConfig config = createConfig().add("max", "2.4");
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(config, createContext()),
                ErrorCode.STRUCTURAL_MISMATCH + "Only 1 integer max parameter value allowed"
        );
    }

    @Test
    @DisplayName("Test with a target node without child nodes")
    public void testTargetNodeWithoutChilds() {
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(createConfig(), createContext()),
                ErrorCode.STRUCTURAL_MISMATCH + "Target node has only 1 child element or none"
        );
    }

    @Test
    @DisplayName("Test with a target node with only 1 child")
    public void testTargetNodeWOneChild() {
        final MutationContext context = createContext(target -> {
            final Document doc = target.getOwnerDocument();
            final Element sub1 = doc.createElement("sub1");
            target.appendChild(sub1);
        });
        assertThrows(MutationException.class, () ->
                        this.generator.generateMutations(createConfig(), context),
                ErrorCode.STRUCTURAL_MISMATCH + "Target node has only 1 child element or none"
        );
    }

    @Test
    @DisplayName("Test with a default max param")
    public void testDefaultMaxParam() {
        final MutationContext context = createContext(target -> {
            final Document doc = target.getOwnerDocument();
            final Element sub1 = doc.createElement("sub1");
            final Element sub2 = doc.createElement("sub2");
            final Element sub3 = doc.createElement("sub3");
            target.appendChild(sub1);
            target.appendChild(sub2);
            target.appendChild(sub3);
        });
        final List<Mutation> mutations = this.generator.generateMutations(createConfig(), context);
        assertThat(mutations).hasSize(5);
    }

    @Test
    @DisplayName("Test with a max param bigger than the possible permutations")
    public void testMaxParamBiggerThanPossible() {
        final MutationConfig config = createConfig().add("max", "7");
        final MutationContext context = createContext(target -> {
            final Document doc = target.getOwnerDocument();
            final Element sub1 = doc.createElement("sub1");
            final Element sub2 = doc.createElement("sub2");
            final Element sub3 = doc.createElement("sub3");
            target.appendChild(sub1);
            target.appendChild(sub2);
            target.appendChild(sub3);
        });
        final List<Mutation> mutations = this.generator.generateMutations(config, context);
        assertThat(mutations).hasSize(5);
    }

    @Test
    @DisplayName("Test with a max param smaller than the possible permutations")
    public void testMaxParamSmallerThanPossible() {
        final MutationConfig config = createConfig().add("max", "4");
        final MutationContext context = createContext(target -> {
            final Document doc = target.getOwnerDocument();
            final Element sub1 = doc.createElement("sub1");
            final Element sub2 = doc.createElement("sub2");
            final Element sub3 = doc.createElement("sub3");
            target.appendChild(sub1);
            target.appendChild(sub2);
            target.appendChild(sub3);
        });
        final List<Mutation> mutations = this.generator.generateMutations(config, context);
        assertThat(mutations).hasSize(4);
    }



}
