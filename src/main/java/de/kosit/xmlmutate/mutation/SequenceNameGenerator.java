package de.kosit.xmlmutate.mutation;

import org.apache.commons.lang3.StringUtils;

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
        final String preprocessed = inputFileName.replace(".xml", "") + "-" + nextValue() + "-" + mutatorName + (StringUtils.isEmpty(postfix) ? "" : "-" + processPostfix(postfix));
        return preprocessed.replace(" ", "-");
    }

    private String processPostfix(final String postfix) {
        final String asciiRemoved = postfix.replaceAll("[^\\x00-\\x7F]", "");
        if (StringUtils.isBlank(asciiRemoved)) {
            return "non-ascii-value";
        } else {
            return asciiRemoved;
        }
    }


}
