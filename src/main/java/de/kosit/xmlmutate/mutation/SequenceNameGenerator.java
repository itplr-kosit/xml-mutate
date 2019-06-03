package de.kosit.xmlmutate.mutation;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    public String generateName(final String docName, final String postfix) {
        return docName + (isNotBlank(docName) ? "-" : "") + nextValue() + (isNotBlank(postfix) ? "-" : "") + postfix;
    }

    @Override
    public String generateName() {
        return Integer.toString(nextValue());
    }
}
