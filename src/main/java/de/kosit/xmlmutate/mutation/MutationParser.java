package de.kosit.xmlmutate.mutation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.lang3.RegExUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.SchematronRuleExpectation.ExpectedResult;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.parser.MutationLexer;
import de.kosit.xmlmutate.mutation.parser.MutationParser.MutatorContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.PropertyContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.SchemaKeywordContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.SchematronKeywordContext;
import de.kosit.xmlmutate.mutator.DefaultMutationGenerator;
import de.kosit.xmlmutate.mutator.MutatorRegistry;
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
            if (ctx.key() == null || ctx.value() == null) {
                throw new IllegalArgumentException("Error parsing property: " + ctx.getText());
            }
            this.config.add(unquote(ctx.key().getText()), unquote(ctx.value().getText()));
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

            String value = ctx.getText();
            log.trace("Value from PI={}", value);
            value = unquote(value);
            value = value.replaceAll("[\\s|\\u00A0]+", " ");
            log.trace("Parse schematron expectation value={}", value);
            // split at least returns the string itself if no split char is found
            String[] rules = value.split(" ");
            String[] ruleParts = null;
            for (int i = 0; i < rules.length; i++) {
                ruleParts = this.parseSchemtronRule(rules[i]);
                if (ruleParts.length == 1) {
                    this.config.addExpectation(
                            new SchematronRuleExpectation(Schematron.DEFAULT_NAME, ruleParts[0],
                                    evaluateExpectedResult(ctx)));
                } else {
                    this.config.addExpectation(
                            new SchematronRuleExpectation(ruleParts[0], ruleParts[1], evaluateExpectedResult(ctx)));
                }
            }

            log.debug("Generated expections num={}", this.config.getSchematronExpectations().size());
            // final String sourceName = COLON_POS > 0 ? value.substring(0, COLON_POS) :
            // Schematron.DEFAULT_NAME;
            // final String ruleName = COLON_POS > 0 ? value.substring(COLON_POS + 1) :
            // value;

        }

        private String[] parseSchemtronRule(String rule) {
            // split at least returns the string itself if no split char is found
            return rule.split(COLON);
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
        final CharStream cs = new ANTLRInputStream(context.getPi().getTextContent());
        final MutationLexer lexer = new MutationLexer(cs);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final de.kosit.xmlmutate.mutation.parser.MutationParser parser = new de.kosit.xmlmutate.mutation.parser.MutationParser(
                tokens);
        final MutationParserListener l = new MutationParserListener(context);
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        parser.addParseListener(l);
        try {
            parser.mutation();
            return new ArrayList<>(l.getMutations());
        } catch (final Exception e) {
            log.error("Error parsing {}", context.getPi().getData(), e);
            return createErrorMutation(context, e.getMessage());
        }
    }

    private List<Mutation> createErrorMutation(final MutationContext context, final String message) {
        final Mutation m = new Mutation(context, Services.getNameGenerator().generateName());
        m.setState(State.ERROR);
        m.setErrorMessage(message);
        return Collections.singletonList(m);
    }

}
