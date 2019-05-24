package de.kosit.xmlmutate.cmd;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.kosit.xmlmutate.mutation.MutationRunner;
import de.kosit.xmlmutate.mutation.RunnerConfig;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.mutation.SequenceNameGenerator;
import de.kosit.xmlmutate.report.TextReportGenerator;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * @author Andreas Penski
 */
public class XmlMutate implements Callable<Integer> {

    @Option(names = { "-t", "--target" }, paramLabel = "FOLDER", description = "The target folder, where artefakts are generated.",
            defaultValue = "target")
    private Path target;

    @Option(names = { "-x", "--schema" }, paramLabel = "*.xsd", description = "The schema, that should be checked.")
    private Path schemaLocation;

    @Option(names = { "-s", "--schematron" }, paramLabel = "MAP", description = "The target folder, where artefakts are generated.")
    private Map<String, Path> schematrons;

    @Parameters(arity = "1..*", description = "Documents to mutate")
    private List<Path> documents;

    public static void main(final String[] args) {
        final int i = CommandLine.call(new XmlMutate(), args);
        System.exit(i);
    }

    @Override
    public Integer call() throws Exception {
        final MutationRunner runner = new MutationRunner(prepareConfig());
        runner.run();
        return 0;
    }

    private RunnerConfig prepareConfig() throws IOException {
        final RunnerConfig config = new RunnerConfig();
        config.setNameGenerator(new SequenceNameGenerator());
        config.setReportGenerator(new TextReportGenerator(new PrintWriter(System.out)));
        config.setDocuments(prepareDocuments());

        // target folder
        if (Files.exists(this.target) && !Files.isWritable(this.target)) {
            throw new IllegalArgumentException("Target folder is not writable");
        }
        Files.createDirectories(this.target);
        config.setTargetFolder(this.target);
        config.setSchematronRules(prepareSchematron());
        return config;
    }

    private List<Schematron> prepareSchematron() {
        return this.schematrons.entrySet().stream().map(e -> new Schematron(e.getKey(), e.getValue().toUri())).collect(Collectors.toList());

    }

    private List<Path> prepareDocuments() {
        final List<Path> available = this.documents.stream().filter(Files::exists).filter(Files::isReadable).collect(Collectors.toList());
        if (available.size() < this.documents.size()) {
            this.documents.removeAll(available);
            throw new IllegalArgumentException(
                    MessageFormat.format("Document {0} does not exist or is not readable", this.documents.get(0)));
        }

        return available.stream().flatMap(this::expandDirectories).filter(e -> e.getFileName().toString().endsWith(".xml"))
                .collect(Collectors.toList());
    }

    private Stream<Path> expandDirectories(final Path path) {
        try {
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Document does not exist: " + path.toAbsolutePath().toString());
            }
            if (Files.isDirectory(path)) {
                return Files.walk(path);
            }
            return Stream.of(path);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Error looking for documents", e);
        }
    }
}
