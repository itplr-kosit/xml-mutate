package de.kosit.xmlmutate.mutation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.apache.commons.lang3.RegExUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.parser.MutationLexer;
import de.kosit.xmlmutate.mutation.parser.MutationParser.MutatorContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.PropertyContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.RuleNameContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.SchemaKeywordContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.SchematronKeywordContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.SchematronRuleContext;
import de.kosit.xmlmutate.mutator.DefaultMutationGenerator;
import de.kosit.xmlmutate.mutator.MutatorRegistry;
import de.kosit.xmlmutate.runner.MutationState;
import de.kosit.xmlmutate.runner.Services;

/**
 * Parser für die Evaluierung von
 * {@link org.w3c.dom.ProcessingInstruction}-Werte der XMUTE-PIs. Der Parser
 * basiert auf einer ANTLR4-Grammatik.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class MutationParser {

    @RequiredArgsConstructor
    public class SchematronRulesParserListener extends de.kosit.xmlmutate.mutation.parser.MutationBaseListener {

        private final de.kosit.xmlmutate.expectation.ExpectedResult expectedResult;

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
     * Integration mit dem ANTLR4-Parser
     */
    @RequiredArgsConstructor
    public class MutationParserListener extends de.kosit.xmlmutate.mutation.parser.MutationBaseListener {

        private static final String COLON = ":";

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
            final boolean valid = "valid".equals(keyword);
            log.debug("Schema {} expectation is={}", keyword, valid);
            this.config.setSchemaValidationAsExpected(valid);
        }

        @Override
        public void exitSchematronKeyword(final SchematronKeywordContext ctx) {
            // replaceAll includes unbreakable spaces too
            final SchematronRulesParserListener l = new SchematronRulesParserListener(evaluateExpectedResult(ctx));
            final List<SchematronRuleExpectation> expectations = parse(
                    unquote(ctx.schematronText().getText()), l, parser -> {
                        parser.schematronRules();
                        return l.getExpectations();
                    }, e -> null);
            expectations.forEach(this.config::addExpectation);
        }

        private ExpectedResult evaluateExpectedResult(final SchematronKeywordContext ctx) {
            return ctx.assertion().getText().equals("valid") ? ExpectedResult.PASS : ExpectedResult.FAIL;
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
                this.mutations = (createErrorMutation(
                        this.context,
                        MessageFormat.format("No valid mutator found for {0}", this.config.getMutatorName())));
            }

            if (this.context.getTarget() == null) {
                this.mutations = createErrorMutation(
                        this.context,
                        MessageFormat.format(
                                "No mutation can be found for {0}. Is PI last " + "element?",
                                this.context.getDocumentName()));
            }
            return this.mutations == null;
        }

    }

    public static class ThrowingErrorListener extends BaseErrorListener {

        public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
                final int charPositionInLine, final String msg, final RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    /**
     * Parsed den gegebenen Kontext
     *
     * @param context
     *                    der Kontext im Dokument; entspricht einer PI
     * @return Liste mit den generierten Mutationen
     */
    public List<Mutation> parse(final MutationContext context) {
        final MutationParserListener listener = new MutationParserListener(context);
        return parse(context.getPi().getTextContent(), listener, parser -> {
            parser.mutation();
            return listener.getMutations();
        }, e -> createErrorMutation(context, e.getMessage()));
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

    private List<Mutation> createErrorMutation(final MutationContext context, final String message) {
        final Mutation m = new Mutation(context, Services.getNameGenerator().generateName());
        m.setState(MutationState.ERROR);
        m.setErrorMessage(message);
        return Collections.singletonList(m);
    }

}
