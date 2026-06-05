package de.kosit.xmlmutate;

import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.runner.FailureMode;
import de.kosit.xmlmutate.runner.RunnerConfig;
import de.kosit.xmlmutate.runner.ValidatorServices;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.validation.Schema;

/**
 * Runner-side test helpers that need orchestrator types (RunnerConfig, ValidatorServices).
 * Kept out of the mutator test-jar so the mutator module stays free of validator/runner deps.
 */
public class RunnerTestHelper {

    public static Schema getBookSchema() {
        return ValidatorServices.getSchemaRepository().createSchema(TestResource.BookResources.SCHEMA);
    }

    public static RunnerConfig createRunnerConfig(final URI documentPath) {
        return RunnerConfig.Builder.forDocuments(getSingleDocument(documentPath)).checkSchema(getBookSchema())
                .checkSchematron(getBookSchematronRules()).targetFolder(createTestTargetFolder("doc/test"))
                .useTransformations(new ArrayList<>()).withFailureMode(FailureMode.FAIL_AT_END).build();
    }

    public static RunnerConfig createSchematronRunnerConfig(final URI documentPath, final List<Schematron> schematronRules,
        final FailureMode failureMode) {
        final RunnerConfig runnerConfig = RunnerConfig.Builder.forDocuments(getSingleDocument(documentPath))
            .checkSchematron(schematronRules).targetFolder(createTestTargetFolder("doc/test"))
            .useTransformations(new ArrayList<>()).withFailureMode(failureMode).saveSvrl(true).build();
        runnerConfig.setIgnoreSchemaInvalidity(true);
        return runnerConfig;
    }

    public static RunnerConfig createRunnerConfig(final URI documentPath, final FailureMode failureMode) {
        final RunnerConfig runnerConfig = createRunnerConfig(documentPath);
        runnerConfig.setFailureMode(failureMode);
        return runnerConfig;
    }

    public static RunnerConfig createRunnerConfig(final URI documentPath, final boolean ignoreSchemainvalidity) {
        final RunnerConfig runnerConfig = createRunnerConfig(documentPath);
        runnerConfig.setIgnoreSchemaInvalidity(ignoreSchemainvalidity);
        return runnerConfig;
    }

    private static List<Path> getSingleDocument(final URI stringPath) {
        final Path path = Paths.get(stringPath);
        return Collections.singletonList(path);
    }

    private static List<Schematron> getBookSchematronRules() {
        final List<Schematron> schematronList = new ArrayList<>();
        final URI uri = TestResource.BookResources.XSL;
        // Only with BR-DE-1 and BR-DE-2 as known rule names
        final List<String> list = Arrays.asList("Book-1", "Book-2");
        final Schematron schematron = new Schematron("schematron", uri, list);
        schematronList.add(schematron);
        return schematronList;
    }

    private static Path createTestTargetFolder(final String path) {
        final Path testPath = Paths.get(path);
        try {
            Files.createDirectories(testPath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (Files.exists(testPath) && !Files.isWritable(testPath)) {
            throw new IllegalArgumentException("Target folder is not writable");
        }
        return testPath;
    }
}
