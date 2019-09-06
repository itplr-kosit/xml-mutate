package de.kosit.xmlmutate.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Parser for xmute {@link org.w3c.dom.ProcessingInstruction} content. The
 * parser is based on antlr4 grammar definition. The parser has no clue about
 * the Document where this {@link org.w3c.dom.ProcessingInstruction} is located.
 *
 * @author Renzo Kottmann
 * @author Andreas Penski
 */

public class XMuteParser {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XMuteParser.class);

    /**
     * Parses the text content of {@link org.w3c.dom.ProcessingInstruction}
     *
     */
    public MutatorInstruction parse(final String data) {

        final CharStream stream = CharStreams.fromString(data);
        final MutatorInstructionLexer lexer = new MutatorInstructionLexer(stream);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final MutatorInstructionParser parser = new MutatorInstructionParser(tokens);

        final MutatorInstructionParserListener listener = new MutatorInstructionParserListener(parser);

        // parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        parser.addParseListener(listener);
        // call to root context starts parsing
        parser.xmute();
        return listener.getInstruction();
    }

    /**
     * Integration with ANTLR4-Parser
     */

    public static class ThrowingErrorListener extends BaseErrorListener {

        public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
                final int charPositionInLine, final String msg, final RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

}
