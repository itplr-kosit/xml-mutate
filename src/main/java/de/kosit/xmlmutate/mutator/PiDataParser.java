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
    private String key = "";
    private String value = "";
    private State currentState = ParserState.START;

    private interface State {
        boolean parse(PiDataParser context);
    }

    private enum ParserState implements State {
        START {

            @Override
            public boolean parse(PiDataParser context) {
                if (context.data == null || context.data.isEmpty()) {
                    context.nextState(ParserState.END);
                    return true;
                }
                context.nextState(ParserState.END);
                return true;
            }
        },
        END {

            /**
             * Currently just stopp parsing, but here for implmenting cleanup
             **/
            @Override
            public boolean parse(PiDataParser context) {
                return false;
            }

        }

    }

    public Map<String, String> parsePiData(String data) {
        this.data = data;
        while (this.state().parse(this)) {
        }

        return dataEntries;
    }

    void nextState(State state) {
        this.currentState = state;
    }

    State state() {
        return currentState;
    }

}
