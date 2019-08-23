package de.kosit.xmlmutate.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.validation.Schema;

import org.fusesource.jansi.AnsiConsole;

import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.NamedTemplate;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.runner.MutationRunner;
import de.kosit.xmlmutate.runner.RunMode;
import de.kosit.xmlmutate.runner.RunnerConfig;
import de.kosit.xmlmutate.runner.Services;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;

/**
 * Base class for command line interface.
 *
 * @author Andreas Penski
 * @author Renzo Kottmann
 */

@Command(description = "XMl-MutaTE: XML Mutation and Test Management tool.", name = "XML Mutate", mixinStandardHelpOptions = true,
         separator = " ")
@Slf4j
public class XmlMutate implements Callable<Integer> {

    @Option(names = { "-o", "--target" }, description = "The target folder, where artifacts are generated.", defaultValue = "target")
    private Path target;

    @Option(names = { "-x", "--schema", "--xsd" }, paramLabel = "*.xsd", description = "The XML Schema file for validation",
            required = true)
    private Path schemaLocation;

    @Option(names = { "-s", "--schematron" }, paramLabel = "MAP", description = "Compiled schematron file(s) for validation")
    private Map<String, Path> schematrons;

    @Option(names = { "-m", "--mode" }, paramLabel = "MODE", description = "The actual processing mode", defaultValue = "ALL")
    private RunMode mode;

    @Option(names = { "-t", "--transformations" }, paramLabel = "MAP",
            description = "Named transformations used for the Transformation-Mutator")
    private Map<String, Path> transformations;

    @Parameters(arity = "1..*", description = "Documents to mutate")
    private List<Path> documents;

    public static void main(final String[] args) {
        AnsiConsole.systemInstall();
        int i = -1;
        try {
            final CommandLine commandLine = new CommandLine(new XmlMutate());
            commandLine.setExecutionExceptionHandler(XmlMutate::logExecutionException);
            i = commandLine.execute(args);
        } catch (final Exception e) {
            System.err.print(e.getMessage());
            System.err.print(";");
            System.err.println("Exit with code=" + i);

        }
        // make sure to have a new line at the end of processing
        System.out.println("\n");
        System.exit(i);
    }

    /**
     * The actual method to call run method on MutationRunner
     */
    @Override
    public Integer call() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final MutationRunner runner = new MutationRunner(prepareConfig(), executor);
        runner.run();
        executor.shutdown();

        return 0;
    }

    /**
     * Prepares a {@link RunnerConfig configuration} for the {@link MutationRunner}.
     */
    private RunnerConfig prepareConfig() throws IOException {
        Files.createDirectories(this.target);
        // target folder
        if (Files.exists(this.target) && !Files.isWritable(this.target)) {
            throw new IllegalArgumentException("Target folder is not writable");
        }
        return RunnerConfig.Builder.forDocuments(prepareDocuments()).targetFolder(this.target).checkSchematron(prepareSchematron())
                .checkSchema(prepareSchema()).useTransformations(prepareTransformations()).build();

    }

    private List<NamedTemplate> prepareTransformations() {
        return this.transformations.entrySet().stream().map(e -> {
            if (Files.exists(e.getValue()) && Files.isReadable(e.getValue())) {
                return new NamedTemplate(e.getKey(), e.getValue());
            }
            throw new IllegalArgumentException(String.format("Provided template '%s' does not exist or is not readable", e.getValue()));
        }).collect(Collectors.toList());
    }

    private Schema prepareSchema() {
        if (this.schemaLocation != null) {
            return Services.getSchemaRepository().createSchema(this.schemaLocation.toUri());
        }
        return null;
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
                throw new IllegalArgumentException("Document or directory does not exist: " + path.toAbsolutePath().toString());
            }
            if (Files.isDirectory(path)) {
                return Files.walk(path);
            }
            return Stream.of(path);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Error looking for input documents", e);
        }
    }

    private static int logExecutionException(final Exception ex, final CommandLine cli, final ParseResult parseResult) {
        System.err.println(ex.getMessage());
        log.error(ex.getMessage(), ex);

        return 1;
    }
}
