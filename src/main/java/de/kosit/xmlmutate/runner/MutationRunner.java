// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
package de.kosit.xmlmutate.runner;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.Result;
import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationDocumentContext;
import de.kosit.xmlmutate.mutation.MutationParser;
import de.kosit.xmlmutate.mutation.Schematron;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import net.sf.saxon.s9api.XdmDestination;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

/**
 * Runner that undertakes the actual processing of the document
 *
 * @author Andreas Penski
 */
public class MutationRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MutationRunner.class);
    private final RunnerConfig configuration;
    private final MutationParser parser;
    private final ExecutorService executorService;
    private final TemplateRepository templateRepository;

    public MutationRunner(final RunnerConfig configuration, final ExecutorService executorService) {
        this.configuration = configuration;
        this.parser = new MutationParser();
        this.executorService = executorService;
        this.templateRepository = Services.getTemplateRepository();
    }

    public RunnerResult run() {
        prepare();
        if (!this.configuration.isIgnoreSchemaInvalidity() && this.configuration.getSchema() != null) {
            checkSchemaValidityOfOriginals(this.configuration.getDocuments());
        }
        final List<Pair<Path, List<Mutation>>> results = this.configuration.getDocuments().stream().map(this::process).map(MutationRunner::awaitTermination).toList();
        this.configuration.getReportGenerator().generate(results, this.configuration.getFailureMode());
        return new RunnerResult(results);
    }

    private void checkSchemaValidityOfOriginals(final List<Path> documents) {
        for (final Path documentPath : documents) {
            try {
                final Document document = ObjectFactory.createDocumentBuilder(false).parse(documentPath.toFile());
                final Result<Boolean, SyntaxError> result = Services.getSchemaValidatonService().validate(this.configuration.getSchema(), document);
                if (result.isInvalid()) {
                    throw new MutationException(ErrorCode.ORIGINAL_XML_NOT_SCHEMA_VALID, documentPath.getFileName().toString(), result.getErrorDescription());
                }
            } catch (final IOException | SAXException e) {
                throw new MutationException(ErrorCode.MUTATION_XML_FILE_READ_PROBLEM);
            }
        }
    }

    private void prepare() {
        // register templates
        if (CollectionUtils.isNotEmpty(this.configuration.getTemplates())) {
            this.configuration.getTemplates().forEach(t -> this.templateRepository.registerTemplate(t.getName(), t.getPath()));
        }
    }

    private static Pair<Path, List<Mutation>> awaitTermination(final Future<Pair<Path, List<Mutation>>> pairFuture) {
        try {
            return pairFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e.getCause().getMessage(), e);
        }
    }

    Future<Pair<Path, List<Mutation>>> process(final Path path) {
        return this.executorService.submit(() -> {
            final Document xmlDocument = DocumentParser.readDocument(path, this.configuration.isSaveParsing());
            Map<String, Set<String>> xmlSchematronValidationFailures = runSchematronValidationWithoutMutations(path);
            final List<Mutation> mutations = parseMutations(xmlDocument, path, xmlSchematronValidationFailures);
            // If there is only mutation with an error, we dont need to process it
            if (mutations.size() == 1 && mutations.stream().anyMatch(Mutation::isErroneousOrContainsErrorMessages)) {
                return new ImmutablePair<>(path, mutations);
            } else {
                // Processing erfolgt sortiert nach nesting tiefe (von tief nach hoch)
                // Grund hierfür ist, das durch Entfernen von Knoten möglicherweise PI aus dem
                // Kontext gerissen werden.
                final List<Mutation> sortedMutations = mutations.stream()
                    .sorted(Comparator.comparing(mutation -> mutation.getContext().getLevel(), Comparator.reverseOrder()))
                    .toList();
                final List<Mutation> mutationsProcessed = process(sortedMutations);
                return new ImmutablePair<>(path, mutationsProcessed);
            }
        });
    }

    private Map<String, Set<String>> runSchematronValidationWithoutMutations(Path path) {
        log.debug("{} -> Started schematron validation before applying any mutations...", path.getFileName());
        Map<String, Set<String>> validationFailures = Map.of();
        try {
            final Map<String, XdmDestination> schematronResult = validateDocumentWithSchematron(path);
            writeSvrlOutputToFile(schematronResult);
            validationFailures = findXmlSchematronValidationFailures(
                schematronResult.values());
            return validationFailures;
        } catch (Exception e) {
            log.error(String.format("Failed to generate SVRL report on target: %s", path.getFileName()), e);
            log.error("It is assumed that {} doesn't have any schematron errors without any mutations applied.",
                path.getFileName());
            return Map.of();
        } finally {
            if (validationFailures.isEmpty()) {
                log.debug("No any schematron failures found");
            } else {
                log.debug("{} Schematron rules failed: {}", validationFailures.keySet().size(), validationFailures.keySet());
                log.trace("XPath failures by schematron code: {}", validationFailures);
            }
            log.debug("{} -> Finished schematron validation before applying any mutations.", path.getFileName());
        }
    }

    private void writeSvrlOutputToFile(Map<String, XdmDestination> schematronResult) {
        if (!this.configuration.isSaveSvrl()) {
            return;
        }
        schematronResult.forEach((key, value) -> {
            try {
                Files.writeString(
                    this.configuration.getTargetFolder().resolve(key).toAbsolutePath(),
                    value.getXdmNode().toString());
            } catch (IOException e) {
                log.warn("Could not save SVRL output to file.", e);
            }
        });
    }

    private Map<String, Set<String>> findXmlSchematronValidationFailures(
        Collection<XdmDestination> xdmDestinations) {
        List<Map<String, Set<String>>> multiSchematronAssertionFailures =  xdmDestinations.stream()
            .map(Services.getSchematronValidationService()::findFailuresWithXPaths)
            .toList();
        return mergeSvrlResultsFromMultipleSchematrons(multiSchematronAssertionFailures);
    }

    private Map<String, Set<String>> mergeSvrlResultsFromMultipleSchematrons(
        List<Map<String, Set<String>>> rez) {
        return rez.stream().flatMap(m -> m.entrySet().stream())
            .collect(groupingBy(
                Entry::getKey, flatMapping(entr -> entr.getValue().stream(), toSet())));
    }

    private Map<String, XdmDestination> validateDocumentWithSchematron(Path path) {
        return this.configuration.getSchematronRules().stream()
            .collect(toMap(schematron -> svrlName(path, schematron),
                schematron -> Services.getSchematronValidationService()
                    .validate(path.toUri(), schematron)));
    }

    private String svrlName(Path path, Schematron schematron) {
        return FilenameUtils.getBaseName(schematron.getUri().getPath()) + "_" +
            FilenameUtils.getBaseName(path.toUri().getPath()) + ".svrl";
    }

    private List<Mutation> process(final List<Mutation> mutations) {
        final List<Mutation> mutationsProcessed = new ArrayList<>();
        for (final Mutation mutation : mutations) {
            process(mutation);
            mutationsProcessed.add(mutation);
            if (checkIfStopProcess(mutation)) {
                break;
            }
        }
        return mutationsProcessed;
    }

    private void process(final Mutation mutation) {
        log.info("Running mutation {}", mutation.getIdentifier());
        this.configuration.getActions().forEach(a -> {
            if (mutation.isErroneous()
                && mutation.isSchemaValid()
                && !(a instanceof ResetAction || a instanceof ResetUncommentAction)
                && !(a instanceof MarkMutationAction.RemoveCommentAction)) {
                log.warn("Skipping running the {} with erroneous mutation id = {}.", a.getClass().getSimpleName(), mutation.getIdentifier());
                return;
            }
            try {
                log.debug("Running {} for {}", a.getClass().getSimpleName(), mutation.getIdentifier());
                a.run(mutation);
            } catch (final Exception e) {
                final MutationException mutationException = new MutationException(ErrorCode.ACTION_RUNNER_ERROR, a.getClass().getName(), mutation.getIdentifier());
                log.error(mutationException.getMessage(), e);
                mutation.getMutationErrorContainer().addGlobalErrorMessage(e.getLocalizedMessage() == null ? mutationException : e);
                mutation.setState(State.ERROR);
            }
        });
    }

    List<Mutation> parseMutations(final Document origin, final Path documentPath) {
        return this.parseMutations(origin, documentPath, Map.of());
    }

    List<Mutation> parseMutations(final Document origin, final Path documentPath, final Map<String, Set<String>> svrlFailures) {
        final List<Mutation> all = new ArrayList<>();
        final TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin, NodeFilter.SHOW_PROCESSING_INSTRUCTION, null, true);
        final List<String> alreadyDeclaredIds = new ArrayList<>();
        while (piWalker.nextNode() != null) {
            final ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
            if (pi.getTarget().equals("xmute")) {
                pi.setData(StringUtils.normalizeSpace(pi.getData()));
                final MutationDocumentContext context = new MutationDocumentContext(pi, documentPath, this.configuration.getSavingMode(), svrlFailures);
                final List<Mutation> mutations = this.parser.parse(context);
                checkSchemaSchematronDeclarations(mutations);
                // Check if PI id is duplicated
                if (!mutations.isEmpty()) {
                    final String currentId = mutations.get(0).getConfiguration().getMutationId();
                    if (currentId != null && !alreadyDeclaredIds.contains(currentId)) {
                        alreadyDeclaredIds.add(currentId);
                    } else if (currentId != null) {
                        mutations.forEach(e -> e.getMutationErrorContainer().addGlobalErrorMessage(new MutationException(ErrorCode.ID_ALREADY_DECLARED)));
                    }
                }
                all.addAll(mutations);
                // If Mode FAIL_FAST, stop process with first error mutation
                if (mutations.stream().anyMatch(this::checkIfStopProcess)) {
                    String failedMutationId = findFirstFailedMutationId(mutations);
                    log.warn("Invalid mutation definition identified id = {}. Stop parsing any further mutations within {}.",
                        failedMutationId, documentPath);
                    return all;
                }
            }
        }
        return all;
    }

    private String findFirstFailedMutationId(List<Mutation> mutations) {
        return mutations.stream()
            .filter(this::checkIfStopProcess)
            .findFirst()
            .map(Mutation::getIdentifier)
            .orElse("unknown");
    }

    private void checkSchemaSchematronDeclarations(final List<Mutation> mutations) {
        if ((this.configuration.getSchema() == null) && (mutations.stream().filter(Mutation::isSchemaExpectationSet).findAny().orElse(null) != null)) {
            throw new MutationException(ErrorCode.CLI_ARGUMENT_NOT_PRESENT_BUT_PI_EXPECTATION, "schema");
        }
        if ((this.configuration.getSchematronRules().isEmpty()) && (mutations.stream().filter(Mutation::isSchematronExpectationSet).findAny().orElse(null) != null)) {
            throw new MutationException(ErrorCode.CLI_ARGUMENT_NOT_PRESENT_BUT_PI_EXPECTATION, "schematron");
        }
    }

    private boolean checkIfStopProcess(final Mutation mutation) {
        return this.configuration.getFailureMode() == FailureMode.FAIL_FAST && mutation.isErroneousOrContainsErrorMessages();
    }
}
