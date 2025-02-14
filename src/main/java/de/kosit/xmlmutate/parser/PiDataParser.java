package de.kosit.xmlmutate.parser;

import de.kosit.xmlmutate.observation.OperationStatus;
import de.kosit.xmlmutate.observation.ParsingOperationStatus;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * PiDataParser. A simple FSM based on Enums.
 *
 * @author Renzo Kottmann
 */
public class PiDataParser {

    private final static Logger log = LogManager.getLogger(PiDataParser.class);
    private Map<String, String> dataEntries = new HashMap<String, String>();
    private int idx = 0;
    private String data = "";

    private StringBuffer key = new StringBuffer();
    private StringBuffer value = new StringBuffer();
    private ParsingOperationStatus parsingStatus = new ParsingOperationStatus(OperationStatus.OK, "All good");
    private State currentState = ParserState.START;

    public static Map<String, String> parse(String data) {
        return new PiDataParser().parsePiData(data);
    }

    PiDataParser() {
        // only constructable within package
    }

    Map<String, String> parsePiData(String data) {
        if (data == null || data.isBlank()) {
            this.parsingStatus = new ParsingOperationStatus(OperationStatus.ERROR,
                    "xmute instruction without any data. Please complete");
            nextState(ParserState.END);
            return dataEntries;
        }

        this.data = data.trim();
        log.debug("Parsing PI data:" + data);

        while (this.state() != ParserState.END && canAdvance()) {
            this.state().parse(this);
        }

        return dataEntries;
    }

    public OperationStatus getParsingStatus() {
        return this.parsingStatus;
    }

    boolean hasError() {
        return !this.parsingStatus.isOK();
    }

    private void addEntry() {
        this.addEntry(key.toString(), value.toString());
    }

    private void addEntry(String k, String v) {
        k = k.trim().toLowerCase();
        if (dataEntries.containsKey(k)) {
            log.error("PI already has property with name=" + k);
            this.parsingStatus.add(new ParsingOperationStatus(OperationStatus.WARNING, 0,
                    "PI already has property with name=" + k, null, "idx=" + idx, "Remove duplicate key=" + key));
            return;
        }
        v = v.trim();
        log.trace(String.format("Got key|%s| and value|%s|", k, v));
        this.dataEntries.put(k, v);
        this.key = new StringBuffer();
        this.value = new StringBuffer();
    }

    private char currentChar() {
        log.trace("char=|" + data.charAt(this.idx) + "|");
        return data.charAt(this.idx);
    }

    private boolean canAdvance() {
        return this.idx + 1 < data.length() ? true : false;
    }

    private void advance() {
        this.idx++;
    }

    private void nextState(State state) {
        if (!canAdvance()) {
            this.currentState = ParserState.END;
            return;
        }
        this.currentState = state;
        log.trace(
                String.format(
                        "idx=%s; char=%s; current=%s; next=%s",
                        this.idx, this.data.charAt(this.idx), currentState, state));

    }

    private State state() {
        return currentState;
    }

    private interface State {
        void parse(PiDataParser context);
    }

    private enum ParserState implements State {
        START {
            @Override
            public void parse(PiDataParser context) {
                log.trace(String.format("Parse START idx=%s;data=%s", context.idx, context.data));
                if (context.data == null || context.data.isEmpty()) {
                    context.nextState(ParserState.END);
                    return;
                }
                log.trace("continue");

                context.nextState(ParserState.KEY_START);
            }
        },
        KEY_START {

            @Override
            public void parse(PiDataParser context) {

                log.trace(String.format("State=%s idx=%s", this.toString(),
                        context.idx));

                // while loop eats all whites spaces until it reaches the key
                while (Character.isSpaceChar(context.currentChar())
                        && context.canAdvance()) {
                    context.advance();
                }
                context.nextState(ParserState.KEY);
            }
        },
        KEY {

            @Override
            public void parse(PiDataParser context) {
                log.trace(String.format("State=%s idx=%s and char=%s=", this.toString(), context.idx,
                        context.data.charAt(context.idx)));

                char c;

                while (context.canAdvance()) {
                    c = context.currentChar();

                    switch (c) {

                        case ' ':
                            log.trace("Parser KEY but space indicating value less key");
                            context.addEntry(context.key.toString().trim(), "");
                            context.nextState(ParserState.KEY_START);
                            return;
                        case '=':
                            log.trace(String.format("Parse KEY idx=%s;key=|%s|", context.idx, context.key));
                            context.advance(); // get beyond equal sign
                            context.nextState(ParserState.VALUE_START);
                            return;
                        default:
                            context.key.append(c);
                            break;
                    }
                    context.advance();
                } // case end of pi
                  // we have to add last char
                  // log.trace("End of pi while in key state last " + context.currentChar());
                context.key.append(context.currentChar());
                context.addEntry(context.key.toString().trim(), "");
                context.nextState(ParserState.END);

            }

        },
        VALUE_START {

            @Override
            public void parse(PiDataParser context) {
                char c;

                while (context.canAdvance()) {
                    c = context.currentChar();
                    context.advance();

                    switch (c) {
                        case '=':
                            log.trace("ParserState=VALUE_START but found equal sign");
                            context.parsingStatus.add(new ParsingOperationStatus(OperationStatus.ERROR,
                                    "Illegal equal sign between key name and value", "idx=" + context.idx,
                                    "Properties must be key=\"value\"."));
                            context.nextState(ParserState.ERROR);
                            return;
                        case ' ':

                            break;
                        case '\t':

                            break;

                        case '\"':
                            log.trace("Parse VALUE_START next state is value");

                            context.nextState(ParserState.VALUE);
                            return;
                        default:
                            log.trace("Parse VALUE_START but found text before quotes");
                            context.parsingStatus.add(new ParsingOperationStatus(OperationStatus.ERROR,
                                    "Illegal char between key name and value. Only whitespace allowed.",
                                    "idx=" + context.idx, "Properties must be key=\"value\"."));

                            context.nextState(ParserState.ERROR);
                            return;
                        // break;
                    }

                }
                // should not end without value
                context.parsingStatus.add(new ParsingOperationStatus(OperationStatus.ERROR,
                        "Finished before value ended with a quote", "idx=" + context.idx, "Add quote at end."));
                context.nextState(ParserState.ERROR);
            }

        },
        VALUE {

            @Override
            public void parse(PiDataParser context) {
                log.trace(String.format("Parser VALUE idx=%s and char=%s=", context.idx,
                        context.currentChar()));
                boolean escape = false;
                char c = ' ';
                while (context.canAdvance()) {
                    c = context.currentChar();
                    context.advance();

                    switch (c) {
                        case '\\':
                            escape = true;
                            break;
                        case '\"':
                            if (escape) {
                                context.value.append(c);
                                escape = false;
                            } else {
                                context.addEntry();
                                context.nextState(ParserState.KEY_START);
                                return;
                            }

                            break;
                        default:
                            context.value.append(c);
                            break;
                    }

                } // case end of
                context.addEntry();
                context.nextState(ParserState.END);

            }
        },
        ERROR {
            @Override
            public void parse(PiDataParser context) {
                log.error(context.parsingStatus.getMessage());
                context.nextState(ParserState.END);
            }
        },
        END {
            /**
             * Currently just stopp parsing, but here for implementing cleanup
             **/
            @Override
            public void parse(PiDataParser context) {
                log.trace("Parse END idx=" + context.idx);
            }

        };

    }

}
