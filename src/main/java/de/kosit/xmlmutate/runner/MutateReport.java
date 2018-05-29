package de.kosit.xmlmutate.runner;

import java.io.IOException;
import java.util.List;

import de.kosit.xmlmutate.XMLMutateConfiguration;
import de.kosit.xmlmutate.tester.TestItem;

/**
 * MutateReport
 */
public interface MutateReport {

    public void addConfig(XMLMutateConfiguration config);

    public void write(String directory, String fileName) throws IOException;

    public void setNumDoc(int num);

    public void addToMutationCount(int num);

    public void addDocumentReport(String docName, List<TestItem> testItems);

    public void addSchemaTestSatement(String docName, String statement);
}