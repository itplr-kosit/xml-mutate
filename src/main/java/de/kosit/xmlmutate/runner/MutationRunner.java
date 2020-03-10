package de.kosit.xmlmutate.runner;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.Result;
import de.init.kosit.commons.SyntaxError;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Runner that undertakes the actual processing of the document
 *
 * @author Andreas Penski
 */
@Slf4j
public class MutationRunner {

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
        final List<Pair<Path, List<Mutation>>> results = this.configuration.getDocuments().stream().map(this::process)
                .map(MutationRunner::awaitTermination).collect(Collectors.toList());
        this.configuration.getReportGenerator().generate(results, this.configuration.getFailureMode());
        return new RunnerResult(results);
    }

    private void checkSchemaValidityOfOriginals(final List<Path> documents) {

        for (final Path documentPath : documents) {
            try {
                final Document document = ObjectFactory.createDocumentBuilder(false).parse(documentPath.toFile());
                final Result<Boolean, SyntaxError> result = Services.getSchemaValidatonService().validate(this.configuration.getSchema(),
                        document);
                if (result.isInvalid()) {
                    throw new MutationException(ErrorCode.ORIGINAL_XML_NOT_SCHEMA_VALID, documentPath.getFileName().toString());
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
            final Document d = DocumentParser.readDocument(path);
            final List<Mutation> mutations = parseMutations(d, path);

            // If there is only mutation with an error, we dont need to process it
            if (mutations.size() == 1 && mutations.stream().anyMatch(Mutation::isErroneousOrContainsErrorMessages)) {
                return new ImmutablePair<>(path, mutations);
            } else {
                // Processing erfolgt sortiert nach nesting tiefe (von tief nach hoch)
                // Grund hierfür ist, das durch Entfernen von Knoten möglicherweise PI aus dem
                // Kontext gerissen werden.
                final List<Mutation> sorted = mutations.stream()
                        .sorted(Comparator.comparing(e -> e.getContext().getLevel(), Comparator.reverseOrder()))
                        .collect(Collectors.toList());
                final List<Mutation> mutationsProcessed = process(sorted);
                return new ImmutablePair<>(path, mutationsProcessed);
            }
        });

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
            if (mutation.isErroneous() && !(a instanceof ResetAction) && !(a instanceof MarkMutationAction.RemoveCommentAction)) {
                return;
            }

            try {
                log.debug("Running {} for {}", a.getClass().getSimpleName(), mutation.getIdentifier());
                a.run(mutation);
            } catch (final Exception e) {
                final MutationException mutationException = new MutationException(ErrorCode.ACTION_RUNNER_ERROR, a.getClass().getName(),
                        mutation.getIdentifier());
                log.error(mutationException.getMessage(), e);
                mutation.getMutationErrorContainer().addGlobalErrorMessage(e.getLocalizedMessage() == null ? mutationException : e);
                mutation.setState(State.ERROR);
            }

        });
    }

    List<Mutation> parseMutations(final Document origin, final Path documentPath) {
        final List<Mutation> all = new ArrayList<>();
        final TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin, NodeFilter.SHOW_PROCESSING_INSTRUCTION, null,
                true);
        final List<String> alreadyDeclaredIds = new ArrayList<>();
        boolean stopParsing = false;
        while (piWalker.nextNode() != null && !stopParsing) {
            final ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
            if (pi.getTarget().equals("xmute")) {
                pi.setData(StringUtils.normalizeSpace(pi.getData()));
                final MutationContext context = new MutationContext(pi, documentPath);
                final List<Mutation> mutations = this.parser.parse(context);
                checkSchemaSchematronDeclarations(mutations);
                // Check if PI id is duplicated
                if (!mutations.isEmpty()) {
                    final String currentId = mutations.get(0).getConfiguration().getMutationId();
                    if (currentId != null && !alreadyDeclaredIds.contains(currentId)) {
                        alreadyDeclaredIds.add(currentId);
                    } else if (currentId != null) {
                        mutations.forEach(e -> e.getMutationErrorContainer()
                                .addGlobalErrorMessage(new MutationException(ErrorCode.ID_ALREADY_DECLARED)));
                    }
                }
                // If Mode FAIL_FAST, stop process with first error mutation
                stopParsing = mutations.stream().anyMatch(this::checkIfStopProcess);

                all.addAll(mutations);
            }
        }
        return all;
    }

    private void checkSchemaSchematronDeclarations(final List<Mutation> mutations) {

        if ((this.configuration.getSchema() == null)
                && (mutations.stream().filter(Mutation::isSchemaExpectationSet).findAny().orElse(null) != null)) {
            throw new MutationException(ErrorCode.CLI_ARGUMENT_NOT_PRESENT_BUT_PI_EXPECTATION, "schema");
        }

        if ((this.configuration.getSchematronRules().isEmpty())
                && (mutations.stream().filter(Mutation::isSchematronExpectationSet).findAny().orElse(null) != null)) {
            throw new MutationException(ErrorCode.CLI_ARGUMENT_NOT_PRESENT_BUT_PI_EXPECTATION, "schematron");
        }

    }

    private boolean checkIfStopProcess(final Mutation mutation) {
        return this.configuration.getFailureMode() == FailureMode.FAIL_FAST && mutation.isErroneousOrContainsErrorMessages();
    }

}
