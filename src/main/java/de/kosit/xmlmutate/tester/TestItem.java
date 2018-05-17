package de.kosit.xmlmutate.tester;

/**
 * TestReport
 */
public interface TestItem {

    public String of();
    public boolean expected();
    public boolean actual();
    public boolean asExpected();
}