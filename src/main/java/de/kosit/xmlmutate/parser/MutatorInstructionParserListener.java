package de.kosit.xmlmutate.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.util.Arrays;

import de.kosit.xmlmutate.parser.MutatorInstructionParser.MutatorContext;
import de.kosit.xmlmutate.parser.MutatorInstructionParser.MutatorPropertyContext;
import de.kosit.xmlmutate.parser.MutatorInstructionParser.SchemaPropertyContext;
import de.kosit.xmlmutate.parser.MutatorInstructionParser.SchematronPropertyContext;
import de.kosit.xmlmutate.parser.MutatorInstructionParser.XmuteContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MutatorInstructionParserListener extends MutatorInstructionBaseListener {
    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
    // .getLogger(MutationInstructionParserListener.class);
    private MutatorInstructionParser parser;
    private MutatorInstruction instruction;

    public MutatorInstructionParserListener() {
        instruction = new MutatorInstruction();
    }

    public MutatorInstructionParserListener(MutatorInstructionParser parser) {
        this();
        this.parser = parser;
    }

    /**
     * @return the instruction
     */
    public MutatorInstruction getInstruction() {
        return this.instruction;
    }

    @Override
    public void exitMutator(final MutatorContext ctx) {
        final String name = this.unquote(ctx.value().getText());
        log.debug("Creating mutator with name={}", name);
        instruction.createMutator(name);
    }

    @Override
    public void exitMutatorProperty(MutatorPropertyContext ctx) {
        if (ctx.key() == null || ctx.value() == null) {
            throw new IllegalArgumentException("Error parsing property: " + ctx.getText());
        }
        final String key = ctx.key().getText();
        final String value = ctx.value().getText();
        log.trace("Add property k={} v={}", key, value);

        this.instruction.addProperty(key, splitValue(unquote(value)));
    }

    @Override
    public void exitSchemaProperty(SchemaPropertyContext ctx) {
        // final String keyword = ctx.assertion().getText();
        // final boolean valid = "valid".equals(keyword);
        log.trace("Schema {} expectation is={}", ctx, ctx);
        this.instruction.setSchemaValidationAsExpected(true);
    }

    @Override
    public void exitSchematronProperty(SchematronPropertyContext ctx) {
        log.trace("Parsing schematron expectation={} on={}", ctx.assertion().getText(), "no value");
        // ctx.value().getText()
        // replaceAll includes unbreakable spaces too
        // final SchematronRulesParserListener l = new
        // SchematronRulesParserListener(evaluateExpectedResult(ctx));
        // final List<SchematronRuleExpectation> expectations = parse(
        // unquote(ctx.schematronText().getText()), l, parser -> {
        // parser.schematronRules();
        // return l.getExpectations();
        // }, e -> null);
        // expectations.forEach(this.config::addExpectation);
    }

    @Override
    public void exitXmute(final XmuteContext ctx) {
        log.trace("Finished parsing mutator instruction {}", ctx.mutator().value());
    }

    private List<String> splitValue(String value) {
        ArrayList<String> l = new ArrayList<String>();
        if (value == null) {
            return l;
        }
        String[] s = value.split(" ");
        if (Arrays.isNullOrEmpty(s)) {
            return l;
        }
        Collections.addAll(l, s);

        return l;
    }

    private String unquote(String data) {
        log.trace("quoted={}", data);
        if (data == null) {
            return data;
        }
        log.trace("unquoted={}", data.substring(1, data.length() - 1));
        return data.substring(1, data.length() - 1);
    }
}
