package de.kosit.xmlmutate.runner;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationParser;

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

    public MutationRunner(final RunnerConfig configuration, final ExecutorService executorService) {
        this.configuration = configuration;
        this.parser = new MutationParser();
        this.executorService = executorService;
    }

    public void run() {
        final List<Pair<Path, List<Mutation>>> results = this.configuration.getDocuments().stream().map(this::process)
                .map(MutationRunner::awaitTermination).collect(Collectors.toList());
        this.configuration.getReportGenerator().generate(results);

    }

    private static Pair<Path, List<Mutation>> awaitTermination(final Future<Pair<Path, List<Mutation>>> pairFuture) {
        try {
            return pairFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e.getCause().getMessage(), e);
        }
    }

    private Future<Pair<Path, List<Mutation>>> process(final Path path) {
        return this.executorService.submit(() -> {
            final Document d = DocumentParser.readDocument(path);
            final List<Mutation> mutations = parseMutations(d, path.getFileName().toString());
            // Processing erfolgt sortiert nach nesting tiefe (von tief nach hoch)
            // Grund hierfür ist, das durch Entfernen von Knoten möglicherweise PI aus dem
            // Kontext gerissen werden.
            final List<Mutation> sorted = mutations.stream()
                    .sorted(Comparator.comparing(e -> e.getContext().getLevel(), Comparator.reverseOrder()))
                    .collect(Collectors.toList());
            process(sorted);
            return new ImmutablePair<>(path, mutations);
        });

    }

    private void process(final Mutation mutation) {
        log.info("Running mutation {}", mutation.getIdentifier());
        this.configuration.getActions().forEach(a -> {
            if (!mutation.isErroneous()) {
                try {
                    log.debug("Running {} for {}", a.getClass().getSimpleName(), mutation.getIdentifier());
                    a.run(mutation);
                } catch (final MutationException e) {
                    log.error(MessageFormat.format("Error running action {0} in mutation {1} ", a.getClass().getName(),
                            mutation.getIdentifier()), e);
                    mutation.setErrorMessage(e.getLocalizedMessage());
                    mutation.setState(State.ERROR);
                }
            }
        });
    }

    private void process(final List<Mutation> mutations) {
        mutations.forEach(this::process);
    }

    private List<Mutation> parseMutations(final Document origin, final String documentName) {
        final List<Mutation> all = new ArrayList<>();
        final TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin,
                NodeFilter.SHOW_PROCESSING_INSTRUCTION, null, true);
        while (piWalker.nextNode() != null) {
            final ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
            if (pi.getTarget().equals("xmute")) {
                final MutationContext context = new MutationContext(pi, documentName);
                final List<Mutation> mutations = this.parser.parse(context);
                all.addAll(mutations);
            }
        }
        return all;
    }

}
