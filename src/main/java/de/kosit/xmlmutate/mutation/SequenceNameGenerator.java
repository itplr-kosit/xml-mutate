package de.kosit.xmlmutate.mutation;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Andreas Penski
 */
public class SequenceNameGenerator implements NameGenerator {

    private final AtomicInteger value = new AtomicInteger(0);

    private int nextValue() {
        return this.value.incrementAndGet();
    }

    @Override
    public String generateName(final String inputFileName, final String mutatorName, final String postfix) {
        return inputFileName.replace(".xml", "") + "-" + nextValue() + "-" + mutatorName + "-" + postfix;
    }

    @Override
    public String generateName() {
        return Integer.toString(nextValue());
    }

    @Override
    public String generateName(final String inputFileName, final String mutatorName) {
        return  inputFileName.replace(".xml", "") + "-" + nextValue() + "-" + mutatorName;
    }
}
