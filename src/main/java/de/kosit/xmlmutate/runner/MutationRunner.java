package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Runner, der die eigentliche Verarbeitung der Dokument übernimmt.
 *
 * @author Andreas Penski
 */
@Slf4j
public class MutationRunner {

    private final RunnerConfig configuration;

    private final MutationParser parser;

    private final ExecutorService executorService;

    private final TemplateRepository templateRepository;

    @Getter
    private boolean errorPresent;

    public MutationRunner(final RunnerConfig configuration, final ExecutorService executorService) {
        this.configuration = configuration;
        this.parser = new MutationParser();
        this.executorService = executorService;
        this.templateRepository = Services.getTemplateRepository();
    }

    public void run() {
        prepare();
        final List<Pair<Path, List<Mutation>>> results = this.configuration.getDocuments().stream().map(this::process)
                .map(MutationRunner::awaitTermination).collect(Collectors.toList());
        checkIfErrorStatePresent(results);
        this.configuration.getReportGenerator().generate(results, this.configuration.getFailureMode());
    }

    private void checkIfErrorStatePresent(final List<Pair<Path, List<Mutation>>> results) {
        results.forEach(o -> errorPresent = o.getValue().stream().anyMatch(n -> n.getState() == State.ERROR));
    }

    private void prepare() {
        // register templates
        this.configuration.getTemplates()
                .forEach(t -> this.templateRepository.registerTemplate(t.getName(), t.getPath()));
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
            final List<Mutation> mutations = parseMutations(d, path.getFileName().toString());
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
                final MutationException mutationException = new MutationException(ErrorCode.ACTION_RUNNER_ERROR, a.getClass().getName(), mutation.getIdentifier());
                log.error(mutationException.getMessage(), e);
                mutation.getMutationErrorContainer().addGlobalErrorMessage(e.getLocalizedMessage() == null ? mutationException : e);
                mutation.setState(State.ERROR);
            }

        });
    }

    List<Mutation> parseMutations(final Document origin, final String documentName) {
        final List<Mutation> all = new ArrayList<>();
        final TreeWalker piWalker = ((DocumentTraversal) origin)
                .createTreeWalker(origin, NodeFilter.SHOW_PROCESSING_INSTRUCTION, null, true);
        final List<String> alreadyDeclaredIds = new ArrayList<>();
        boolean stopParsing = false;
        while (piWalker.nextNode() != null && !stopParsing) {
            final ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
            if (pi.getTarget().equals("xmute")) {
                final MutationContext context = new MutationContext(pi, documentName);
                List<Mutation> mutations = this.parser.parse(context);
                // Check if PI id is duplicated
                if (!mutations.isEmpty()) {
                    final String currentId = mutations.get(0).getConfiguration().getMutationId();
                    if (currentId != null && !alreadyDeclaredIds.contains(currentId)) {
                        alreadyDeclaredIds.add(currentId);
                    } else if (currentId != null) {
                        mutations.forEach(e -> e.getMutationErrorContainer().addGlobalErrorMessage(new MutationException(ErrorCode.ID_ALREADY_DECLARED)));
                    }
                }
                // If Mode FAIL_FAST, stop process with first error mutation
                stopParsing = mutations.stream().anyMatch(this::checkIfStopProcess);

                all.addAll(mutations);
            }
        }
        return all;
    }

    private boolean checkIfStopProcess(final Mutation mutation) {
        return this.configuration.getFailureMode() == FailureMode.FAIL_FAST
                && mutation.isErroneousOrContainsErrorMessages();
    }

}
