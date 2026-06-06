package de.kosit.xmlmutate.parser;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.mutation.XMuteInstruction;
import de.kosit.xmlmutate.observation.OperationStatus;
import de.kosit.xmlmutate.observation.ParsingOperationStatus;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ProcessingInstruction;

public class MutatorInstructionParser {
    private static Logger log = LoggerFactory
            .getLogger(MutatorInstructionParser.class);

    public MutatorInstructionParser() {
    }

    public XMuteInstruction parseInstruction(ProcessingInstruction pi) {

        ParsingOperationStatus xinStatus = new ParsingOperationStatus();

        // first parse pi data and get defined properties therein
        Properties props = new Properties();
        PiDataParser piParser = new PiDataParser();
        props.putAll(piParser.parsePiData(pi.getData()));

        XMuteInstruction instruction = new XMuteInstruction(pi, props);

        OperationStatus status = piParser.getParsingStatus();

        if (status.isNotOK()) {
            xinStatus.add(status);
            instruction.addOperationStatus(xinStatus);
            log.error("Empty or null Xmute pi with error=" + instruction.hasError());
            return instruction;
        }

        instruction.setSchemaExpectation(parseSchemaExpectation(props, xinStatus));
        return instruction;

    }

    private ExpectedResult parseSchemaExpectation(Properties props, ParsingOperationStatus status) {
        if (props.containsKey(PropertyKeys.SCHEMA_VALID) && props.containsKey(PropertyKeys.SCHEMA_INVALID)) {
            log.error("Can not have both schema-valid  and schema-invalid within same xmute instruction.");
            status.add(new ParsingOperationStatus(OperationStatus.ERROR,
                    "Can not have both schema-valid  and schema-invalid within same xmute instruction."));
            return ExpectedResult.UNDEFINED;
        }
        if (props.containsKey(PropertyKeys.SCHEMA_VALID)) {
            return ExpectedResult.PASS;
        }
        if (props.containsKey(PropertyKeys.SCHEMA_INVALID)) {
            return ExpectedResult.FAIL;
        }
        return ExpectedResult.UNDEFINED;
    }

}
