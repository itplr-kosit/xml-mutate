package de.kosit.xmlmutate.mutator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.transform.Templates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.ProcessingInstruction;

import de.kosit.xmlmutate.tester.SchematronTester;
import de.kosit.xmlmutate.tester.TestExpectation;

/**
 * MutatorParser
 *
 * @author Renzo Kottmann
 */
public class MutatorParser {

    private final static Logger log = LogManager.getLogger(MutatorParser.class);
    public final static String PI_TARGET_NAME = "xmute";

    public static Mutator parse(ProcessingInstruction pi) {
        return parse(pi, null);

    }

    public static Mutator parse(ProcessingInstruction pi, Map<String, Templates> xsltCache) {
        return parse(pi, xsltCache, null);
    }

    public static Mutator parse(ProcessingInstruction pi, Map<String, Templates> xsltCache,
            Map<String, SchematronTester> schematrons) {

        if (Objects.isNull(pi)) {
            throw new MutatorException("Processing Instruction should not be null!!");
        }

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
        // from now we can expect to find a valid mutator name
        PiDataParser pip = new PiDataParser();
        Map<String, String> piData = pip.parsePiData(data);
        MutatorConfig config = parseMutatorConfig(piData);
        String mutatorName = config.getInstructionName();
        log.debug("mutator name equals={}", mutatorName);

        Mutator mutator = null;
        switch (mutatorName) {
        case "empty":
            mutator = new EmptyMutator(config);
            break;
        case "remove":
            mutator = new RemoveMutator(config);
            break;
        case "add":
            mutator = new AddElementMutator(config, xsltCache.get(mutatorName));
            break;
        // case "ch-txt":
        // mutator = new ChangeTextMutator(config);
        // break;
        default:
            throw new MutatorException("No valid mutator name given!");
            // break;
        }
        return mutator;
    }

    private static Map<String, String> parsePiData(String data) {
        Map<String, String> dataEntries = new HashMap<String, String>();
        String[] entries = data.split("\\s+");
        final String ENTRY_SEP = "=";
        String entry = "";
        String key = "";
        String val = "";
        int sep_idx = 0;
        for (int i = 0; i < entries.length; i++) {
            entry = entries[i].trim();
            log.trace("entry string:{}", entry);

            if (entry.contains(ENTRY_SEP)) {
                sep_idx = entry.indexOf(ENTRY_SEP);
                key = entry.substring(0, sep_idx).trim().toLowerCase();
                val = entry.substring(sep_idx + 1);
                val = val.replace("\"", "");
            } else {
                key = entry.trim().toLowerCase();
                val = "";
            }
            log.trace("Entry key={} : val={}", key, val);
            dataEntries.put(key, val);
        }
        return dataEntries;
    }

    private static MutatorConfig parseMutatorConfig(Map<String, String> dataEntries) {
        MutatorConfigImpl config = new MutatorConfigImpl();
        parseSchemaValid(dataEntries, config);
        parseSchematron(dataEntries, config);
        parseSchematronInvalid(dataEntries, config);
        config.setInstructionName(dataEntries.get("mutator"));
        return config;

    }

    private static String getDataValue(Map<String, String> dataEntries, String key) {
        String value = dataEntries.get(key);
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    private static void parseSchematron(Map<String, String> dataEntries, MutatorConfigImpl config) {
        if (getDataValue(dataEntries, "schematron-valid").isEmpty()) {
            return;
        }
        String rulesEntry = dataEntries.get("schematron-valid");

        String[] rules = rulesEntry.split(",");

        String rule = "";
        String on = "";
        String what = "";
        int sep_idx = 0;

        for (int i = 0; i < rules.length; i++) {
            rule = rules[i];
            sep_idx = rule.indexOf(":");

            if (sep_idx > -1) {
                on = rule.substring(0, sep_idx).trim().toLowerCase();
                what = rule.substring(sep_idx + 1).trim().toLowerCase();
            } else {
                what = rule.trim().toLowerCase();
            }
            config.addSchematronExpectation(new TestExpectation(on, what, true));
        }

    }

    private static void parseSchematronInvalid(Map<String, String> dataEntries, MutatorConfigImpl config) {
        if (getDataValue(dataEntries, "schematron-invalid").isEmpty()) {
            return;
        }
        String rulesEntry = dataEntries.get("schematron-invalid");

        String[] rules = rulesEntry.split(",");

        String rule = "";
        String on = "";
        String what = "";
        int sep_idx = 0;

        for (int i = 0; i < rules.length; i++) {
            rule = rules[i];
            sep_idx = rule.indexOf(":");

            if (sep_idx > -1) {
                on = rule.substring(0, sep_idx).trim().toLowerCase();
                what = rule.substring(sep_idx + 1).trim().toLowerCase();
            } else {
                what = rule.trim().toLowerCase();
            }
            config.addSchematronExpectation(new TestExpectation(on, what, false));
        }

    }

    private static void parseSchemaValid(Map<String, String> dataEntries, MutatorConfigImpl config) {

        if (getDataValue(dataEntries, "schema-valid").isEmpty()) {
            config.setExpectSchemaValid(true);
        } else {
            // strictly speaking this elese is not necessary cause true is default
            config.setExpectSchemaValid(Boolean.valueOf(dataEntries.get("schema-valid")));
        }
    }

}