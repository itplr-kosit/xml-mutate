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
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.parser.MutationLexer;
import de.kosit.xmlmutate.mutation.parser.MutationParser.MutatorContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.PropertyContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.SchemaKeywordContext;
import de.kosit.xmlmutate.mutation.parser.MutationParser.SchematronKeywordContext;

/**
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class MutationParser {

    @RequiredArgsConstructor
    public class MutationParserListener extends de.kosit.xmlmutate.mutation.parser.MutationBaseListener {

        private final MutationContext context;

        private final MutatorRegistry registry = MutatorRegistry.getInstance();

        private final MutationConfig config = new MutationConfig();

        @Getter
        private List<Mutation> mutations;

        @Override
        public void exitProperty(final PropertyContext ctx) {
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
            this.config.setExpectSchemaValid(StringUtils.equalsIgnoreCase("valid", ctx.getText()));
        }

        @Override
        public void exitSchematronKeyword(final SchematronKeywordContext ctx) {
            // TODO muss noch umgesetzt werden
        }

        @Override
        public void exitMutation(final de.kosit.xmlmutate.mutation.parser.MutationParser.MutationContext ctx) {
            if (validate()) {
                MutationGenerator generator = MutatorRegistry.getGenerator(this.config.getMutatorName());
                if (generator == null) {
                    generator = MutatorRegistry.getGenerator(DefaultMutationGenerator.NAME);
                }
                this.mutations = generator.generateMutations(this.config, this.context);
            }
        }

        private boolean validate() {
            if (this.config.getMutatorName() == null || MutatorRegistry.getMutator(this.config.getMutatorName()) == null) {
                this.mutations = (createErroMutation(this.context,
                        MessageFormat.format("No valid mutator found for {0}", this.config.getMutatorName())));
            }

            if (this.context.getTarget() == null) {
                this.mutations = createErroMutation(this.context,
                        MessageFormat.format("No mutation can be found for {0}. Is PI last " + "element?", this.context.getDocumentName()));
            }
            return this.mutations == null;
        }

    }

    public static class ThrowingErrorListener extends BaseErrorListener {

        public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
                final int charPositionInLine, final String msg, final RecognitionException e) throws ParseCancellationException {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    private final NameGenerator nameGenerator;

    /**
     * Parsed den gegebenen Kontext
     *
     * @param context der Kontext im Dokument; entspricht einer PI
     * @return Liste mit den generierten Mutationen
     */
    public List<Mutation> parse(final MutationContext context) {
        final CharStream cs = new ANTLRInputStream(context.getPi().getTextContent());
        final MutationLexer lexer = new MutationLexer(cs);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final de.kosit.xmlmutate.mutation.parser.MutationParser parser = new de.kosit.xmlmutate.mutation.parser.MutationParser(tokens);
        final MutationParserListener l = new MutationParserListener(context);
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        parser.addParseListener(l);
        try {
            parser.mutation();
            return new ArrayList<>(l.getMutations());
        } catch (final Exception e) {
            log.error("Error parsing {}", context.getPi().getData(), e);
            return createErroMutation(context, e.getMessage());
        }
    }

    private List<Mutation> createErroMutation(final MutationContext context, final String message) {
        final Mutation m = new Mutation(context, MutationParser.this.nameGenerator.generateName());
        m.setState(State.ERROR);
        m.setErrorMessage(message);
        return Collections.singletonList(m);
    }

}
