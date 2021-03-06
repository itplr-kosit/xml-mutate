package de.kosit.xmlmutate.mutation;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronEnterity;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.parser.MutationLexer;
import de.kosit.xmlmutate.mutation.parser.MutationParser.*;
import de.kosit.xmlmutate.mutator.DefaultMutationGenerator;
import de.kosit.xmlmutate.mutator.MutatorRegistry;
import de.kosit.xmlmutate.runner.MutationException;
import de.kosit.xmlmutate.runner.Services;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Parser for the evaluation of
 * {@link org.w3c.dom.ProcessingInstruction} values of the XMUTE-PIs.
 * The parser is based on a ANTLR4 grammar.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class MutationParser {

    @RequiredArgsConstructor
    public static class SchematronRulesParserListener extends de.kosit.xmlmutate.mutation.parser.MutationBaseListener {

        private final ExpectedResult expectedResult;

        @Getter(AccessLevel.PACKAGE)
        private final List<SchematronRuleExpectation> expectations = new ArrayList<>();

        @Override
        public void exitSchematronRule(final SchematronRuleContext ctx) {
            final String text = ctx.schematronName() != null ? ctx.schematronName().getText() : null;
            final String ruleName = ctx.ruleName().stream().map(RuleNameContext::getText).collect(Collectors.joining());
            this.expectations.add(new SchematronRuleExpectation(text, ruleName, this.expectedResult));
        }
    }

    /**
     * Integration with the ANTLR4 parser
     */
    @RequiredArgsConstructor
    public class MutationParserListener extends de.kosit.xmlmutate.mutation.parser.MutationBaseListener {

        private final MutationContext context;

        private final MutationConfig config = new MutationConfig();

        @Getter
        private List<Mutation> mutations;

        @Override
        public void exitProperty(final PropertyContext ctx) {
            if (ctx.identifier() == null || ctx.value() == null) {
                throw new IllegalArgumentException("Error parsing property: " + ctx.getText());
            }
            this.config.add(unquote(ctx.identifier().getText()), unquote(ctx.value().getText()));
        }

        @Override
        public void exitMutator(final MutatorContext ctx) {
            if (ctx.name() != null) {
                this.config.setMutatorName(unquote(ctx.name().getText()));
            }
        }

        private String unquote(final String text) {
            return RegExUtils.removeAll(text, "\"");
        }

        @Override
        public void exitSchemaKeyword(final SchemaKeywordContext ctx) {
            final String keyword = ctx.assertion().getText();
            log.debug("Schema expectation is={}", keyword);
            this.config.setSchemaValidationExpectation("valid".equalsIgnoreCase(keyword) ? ExpectedResult.PASS : ExpectedResult.FAIL);
        }

        @Override
        public void exitSchematronKeyword(final SchematronKeywordContext ctx) {
            // replaceAll includes unbreakable spaces too
            final SchematronRulesParserListener l = new SchematronRulesParserListener(evaluateExpectedResult(ctx));
            if (ctx.entirity() == null) {
                try {
                    final List<SchematronRuleExpectation> expectations = parse(
                            unquote(ctx.schematronText().getText()), l, parser -> {
                                parser.schematronRules();
                                return l.getExpectations();
                            }, e -> null);
                    expectations.forEach(this.config::addExpectation);
                } catch (final Exception e) {
                    if (ctx.schematronText() != null) {
                        throw new MutationException(ErrorCode.SCHEMATRON_RULE_DEFINITION_ERROR, unquote(ctx.schematronText().getText()));
                    } else {
                        throw new MutationException(ErrorCode.SCHEMATRON_KEYWORD_ERROR);
                    }
                }
            } else {
                this.config.setSchematronEnterityExpectation(Pair.of(evaluateEnterity(ctx), evaluateExpectedResult(ctx)));
            }
        }

        private ExpectedResult evaluateExpectedResult(final SchematronKeywordContext ctx) {
            return ctx.assertion().getText().equals("valid") ? ExpectedResult.PASS : ExpectedResult.FAIL;
        }

        private SchematronEnterity evaluateEnterity(final SchematronKeywordContext ctx) {
            return ctx.entirity().getText().equals("all") ? SchematronEnterity.ALL : SchematronEnterity.NONE;
        }


        @Override
        public void exitTagKeyword(final TagKeywordContext ctx) {
            if (ctx.tagText() != null) {
                final String tag = StringUtils.deleteWhitespace(unquote(ctx.tagText().getText()));
                if (tag.length() == 0) {
                    throw new MutationException(ErrorCode.TAG_CONTENT_EMPTY);
                }
                final List<String> tagNames = Arrays.asList(tag.split(","));
                tagNames.forEach(e -> this.config.getTagNames().add(e));
            }
        }

        @Override
        public void exitIdKeyword(final IdKeywordContext ctx) {
            if (ctx.idText() != null) {
                final String id = unquote(ctx.idText().getText());
                if (StringUtils.deleteWhitespace(id).length() == 0) {
                    throw new MutationException(ErrorCode.ID_CONTENT_EMPTY);
                }
                if (id.contains(",") || id.contains(" ")) {
                    throw new MutationException(ErrorCode.MORE_THAN_ONE_ID);
                } else {
                    this.config.setMutationId(unquote(ctx.idText().getText()));
                }
            }
        }

        @Override
        public void exitMutation(final de.kosit.xmlmutate.mutation.parser.MutationParser.MutationContext ctx) {
            if (validate()) {
                final MutatorRegistry registry = Services.getRegistry();
                MutationGenerator generator = registry.getGenerator(this.config.getMutatorName());
                if (generator == null) {
                    generator = registry.getGenerator(DefaultMutationGenerator.NAME);
                }
                this.mutations = generator.generateMutations(this.config, this.context);
            }
        }


        private boolean validate() {
            if (this.config.getMutatorName() == null
                    || Services.getRegistry().getMutator(this.config.getMutatorName()) == null) {
                this.mutations = createErrorMutation(
                        this.context, new MutationException(ErrorCode.NO_MUTATOR_FOUND, this.config.getMutatorName()));
            }

            if (this.context.getTarget() == null) {
                this.mutations = createErrorMutation(
                        this.context, new MutationException(ErrorCode.NO_MUTATION_FOUND, this.context.getDocumentName()));
            }
            return this.mutations == null;
        }

    }

    public static class ThrowingErrorListener extends BaseErrorListener {

        static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
                                final int charPositionInLine, final String msg, final RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    /**
     * Parses the given context
     *
     * @param context the document context; corresponds to a PI
     * @return the list with the generated mutations
     */
    public List<Mutation> parse(final MutationContext context) {
        final MutationParserListener listener = new MutationParserListener(context);
        return parse(context.getPi().getTextContent(), listener, parser -> {
            parser.mutation();
            return listener.getMutations();
        }, e -> createErrorMutation(context, e));
    }

    private static <T> T parse(final String text, final ParseTreeListener listener,
                               final Function<de.kosit.xmlmutate.mutation.parser.MutationParser, T> function,
                               final Function<Exception, T> errorFunction) {
        final CharStream cs = new ANTLRInputStream(text);
        final MutationLexer lexer = new MutationLexer(cs);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final de.kosit.xmlmutate.mutation.parser.MutationParser parser = new de.kosit.xmlmutate.mutation.parser.MutationParser(
                tokens);

        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        parser.addParseListener(listener);

        try {
            return function.apply(parser);
        } catch (final Exception e) {
            log.error("Error parsing {}", text, e);
            return errorFunction.apply(e);
        }

    }

    private List<Mutation> createErrorMutation(final MutationContext context, final Exception exception) {
        final Mutation m = new Mutation(context, Services.getNameGenerator().generateName(context.getDocumentName(), "error", null));
        m.setState(State.ERROR);
        m.getMutationErrorContainer().addGlobalErrorMessage(exception);
        return Collections.singletonList(m);
    }

}
