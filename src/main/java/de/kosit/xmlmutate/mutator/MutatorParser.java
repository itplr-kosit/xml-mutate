package de.kosit.xmlmutate.mutator;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.ProcessingInstruction;

/**
 * MutatorParser
 * @author Renzo Kottmann
 */
public class MutatorParser {

    private final static Logger log = LogManager.getLogger(MutatorParser.class);
    public final static String PI_TARGET_NAME = "xmute";
    private final static Map<String,String> dataEntries = new HashMap<String, String>();

    public static Mutator parse(ProcessingInstruction pi) {
        //TODO throw exception in case pi is null

        if (pi.getTarget() == null || "".equals(pi.getTarget())) {
            throw new MutatorException("Processing Instruction has no target!!");
        }

        String target = pi.getTarget().toLowerCase().trim();

        if (!target.equals(MutatorParser.PI_TARGET_NAME)) {
            throw new MutatorException("This is not a xmute Processing Instruction !!");
        }

        String data = pi.getData();
        if (data == null || "".equals(data)) {
            throw new MutatorException("Processing Instruction has no data!!");
        }
        //from now we can expect to find a valid mutator name
        parsePiData(data);
        String mutatorName = parseMutatorName().toLowerCase();
        log.debug("mutator name equals={}",mutatorName);
        MutatorConfig config = parseMutatorConfig(data);
        Mutator mutator = null;
        switch (mutatorName) {
        case "empty":
            mutator = new EmptyMutator(config);
            break;

        default:
            throw new MutatorException("No valid mutator name given!");
            //break;
        }
        return mutator;
    }

    private static void parsePiData(String data) {
      
        String[] entries = data.split("\"\\s+");
        final String ENTRY_SEP = "=";
        String entry = "";
        String key = "";
        String val = "";
        int sep_idx = 0;
        for (int i = 0; i < entries.length; i++) {
            entry = entries[i].trim();
            log.debug("entry string={}", entry);
            
            if (entry.contains( ENTRY_SEP )) {
                sep_idx = entry.indexOf( ENTRY_SEP );
                key = entry.substring(0, sep_idx).trim().toLowerCase();
                val = entry.substring(sep_idx + 1);
                val = val.replace("\"", "");
            }
            log.debug("Entry key={} : val={}", key, val);
            dataEntries.put(key, val);
        }
    }

    private static String parseMutatorName() throws MutatorException {

        return (String) dataEntries.get("mutator").trim().toLowerCase();
    }

    private static MutatorConfig parseMutatorConfig(String data) {

        return new MutatorConfigImpl();

    }

}