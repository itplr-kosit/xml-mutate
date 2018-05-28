package de.kosit.xmlmutate.mutator;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * PiDataParser
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
    private State currentState = ParserState.START;

    private interface State {
        boolean parse(PiDataParser context);
    }

    private enum ParserState implements State {
        START {
            @Override
            public boolean parse(PiDataParser context) {
                log.debug(String.format("Parse START idx=%s;data=%s", context.idx, context.data));
                if (context.data == null || context.data.isEmpty()) {
                    context.nextState(ParserState.END);
                    return true;
                }
                context.nextState(ParserState.KEY_START);

                return true;
            }
        },
        KEY_START {

            @Override
            public boolean parse(PiDataParser context) {
                log.debug(String.format("State=%s idx=%s and char=%s=", this.toString(), context.idx,
                        context.data.charAt(context.idx)));

                char l = context.data.charAt(context.idx);

                for (; context.idx < context.data.length(); context.idx++) {
                    l = context.data.charAt(context.idx);
                    if (!Character.isSpaceChar(l)) {
                        context.nextState(ParserState.KEY);
                        return true;
                    }

                }
                return false;
            }
        },
        KEY

        {

            @Override
            public boolean parse(PiDataParser context) {
                log.debug(String.format("State=%s idx=%s and char=%s=", this.toString(), context.idx,
                        context.data.charAt(context.idx)));
                boolean escape = false;
                char l = ' ';
                for (; context.idx < context.data.length(); context.idx++) {
                    l = context.data.charAt(context.idx);
                    switch (l) {
                    case '\\':
                        escape = true;
                        break;
                    case ' ':
                        if (escape) {
                            escape = false;
                            context.key.append(l);
                        } else {
                            log.debug("Parser KEY but space indicating value less key");
                            context.addEntry(context.key.toString().trim(), "");
                            context.nextState(ParserState.KEY_START);
                            return true;
                        }
                        break;
                    case '=':
                        if (escape) {
                            escape = false;
                            context.key.append(l);
                        } else {
                            log.debug(String.format("Parse KEY idx=%s;key=%s=", context.idx, context.key));
                            context.nextState(ParserState.VALUE_START);
                            return true;
                        }

                    default:
                        escape = false;
                        context.key.append(l);
                        break;
                    }
                }
                // if no other key is found after idx is exhausted
                context.addEntry(context.key.toString().trim(), "");
                context.nextState(ParserState.END);
                return true;
            }

        },
        VALUE_START {

            @Override
            public boolean parse(PiDataParser context) {
                log.debug(String.format("Parser VALUE_START idx=%s and char=%c=", context.idx,
                        context.data.charAt(context.idx)));
                char l = ' ';
                for (; context.idx < context.data.length(); context.idx++) {
                    l = context.data.charAt(context.idx);

                    switch (l) {
                    case '=':

                        log.debug("Parse VALUE_START, but still have =");
                        break;
                    case ' ':

                        break;
                    case '\t':

                        break;

                    case '"':
                        log.debug("Parse VALUE_START next state is value");
                        context.idx++;
                        context.nextState(ParserState.VALUE);
                        return true;
                    // break;
                    default:
                        break;
                    }

                }
                return false;
            }

        },
        VALUE {

            @Override
            public boolean parse(PiDataParser context) {
                log.debug(String.format("Parser VALUE idx=%s and char=%s=", context.idx,
                        context.data.charAt(context.idx)));
                boolean escape = false;
                char l = ' ';
                for (; context.idx < context.data.length(); context.idx++) {
                    l = context.data.charAt(context.idx);

                    switch (l) {
                    case '\\':
                        escape = true;
                        break;
                    case '"':
                        if (escape) {
                            context.value.append(l);
                        } else {
                            context.addEntry(context.key.toString().trim(), context.value.toString().trim());
                            if (++context.idx < context.data.length()) {
                                context.nextState(ParserState.KEY_START);
                            } else {
                                context.nextState(ParserState.END);
                            }
                            return true;
                        }

                        break;
                    default:
                        context.value.append(l);
                        break;
                    }
                }
                // idx is only after value allowed to be exhausted
                context.nextState(ParserState.END);
                return true;
            }

        },
        END {

            /**
             * Currently just stopp parsing, but here for implementing cleanup
             **/
            @Override
            public boolean parse(PiDataParser context) {
                log.debug("Parse END idx=" + context.idx);
                return false;
            }

        };

        // public String toString() {
        // return name();
        // }

    }

    public Map<String, String> parsePiData(String data) {
        if (data == null || data.isEmpty()) {
            nextState(ParserState.END);
            return dataEntries;
        }

        this.data = data.trim();
        log.debug("Parsing data:" + data);
        while (this.state().parse(this)) {
        }

        return dataEntries;
    }

    private void addEntry(String k, String v) {
        k = k.toString().trim();
        v = v.toString().trim();
        log.debug(String.format("Parser %s got key=%s= and value=%s=", this.currentState, k, v));
        this.dataEntries.put(k, v);
        this.key = new StringBuffer();
        this.value = new StringBuffer();
    }

    private void nextState(State state) {
        if (idx < this.data.length()) {
            this.currentState = state;
        } else {
            this.currentState = ParserState.END;
        }

    }

    private State state() {
        return currentState;
    }

}
