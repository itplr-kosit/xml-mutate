package de.kosit.xmlmutate.runner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.kosit.xmlmutate.XMLMutateConfiguration;
import de.kosit.xmlmutate.tester.TestItem;

/**
 * MutateReportText
 */
public class MutateReportText implements MutateReport {

    private XMLMutateConfiguration config = null;
    private int numDocs = 0;
    private int numMutations = 0;

    private Map<String, List<TestItem>> docReports = new HashMap<String, List<TestItem>>();

    private Map<String, String> schemaTestStatements = new HashMap<String, String>();

    @Override
    public void addConfig(XMLMutateConfiguration config) {
        this.config = config;
    }

    @Override
    public void write(String directory, String fileName) throws IOException {
        Path path = Paths.get(directory, fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(String.format("Generated %s mutations from %s original documents.", numMutations, numDocs));

            for (String docName : docReports.keySet()) {
                writer.newLine();
                writer.write("Mutation " + docName);
                writer.newLine();
                writer.newLine();
                writer.write("Schema Test: ");
                writer.write(schemaTestStatements.get(docName));
                writer.newLine();
                writer.write("Schema Tests: ");
                writer.newLine();
                for (TestItem item : docReports.get(docName)) {
                    writer.write(item.toString());
                    writer.newLine();
                }
            }

        }
    }

    @Override
    public void setNumDoc(int num) {
        this.numDocs = num;
    }

    @Override
    public void addToMutationCount(int num) {
        this.numMutations = this.numMutations + num;
    }

    @Override
    public void addDocumentReport(String docName, List<TestItem> testItems) {
        docReports.put(docName, testItems);
    }

    @Override
    public void addSchemaTestSatement(String docName, String statement) {
        schemaTestStatements.put(docName, statement);
    }

}