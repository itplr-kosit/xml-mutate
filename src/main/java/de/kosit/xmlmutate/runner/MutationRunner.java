package de.kosit.xmlmutate.runner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;

import de.init.kosit.commons.ObjectFactory;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.mutation.MutationParser;

/**
 * @author Andreas Penski
 */
@Slf4j
public class MutationRunner {

    private final RunnerConfig configuration;

    private final MutationParser parser;

    private final ExecutorService executorService;

    public MutationRunner(final RunnerConfig configuration, final ExecutorService executorService) {
        this.configuration = configuration;
        this.parser = new MutationParser(configuration.getNameGenerator());
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
            throw new IllegalStateException("Error awaiting result", e);
        }
    }

    private Future<Pair<Path, List<Mutation>>> process(final Path path) {
        return this.executorService.submit(() -> {
            final Document d = readDocument(path);
            final List<Mutation> mutations = parseMutations(d, path.getFileName().toString());
            // Processing erfolgt sortiert nach nesting tiefe (von tief nach hoch)
            process(mutations.stream().sorted(Comparator.comparing(e -> e.getContext().getLevel(), Comparator.reverseOrder()))
                    .collect(Collectors.toList()));
            return new ImmutablePair<>(path, mutations);
        });

    }

    private void process(final Mutation mutation) {
        this.configuration.getActions().forEach(a -> {
            if (!mutation.isErroneous()) {
                log.info("Running mutation {}", mutation.getIdentifier());
                try {
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
        final TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin, NodeFilter.SHOW_PROCESSING_INSTRUCTION, null,
                true);
        while (piWalker.nextNode() != null) {
            final ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
            if (pi.getTarget().equals("xmute")) {
                final MutationContext context = createContext(documentName, pi);
                final List<Mutation> mutations = this.parser.parse(context);
                all.addAll(mutations);
            }
        }
        return all;
    }

    private static MutationContext createContext(final String documentName, final ProcessingInstruction pi) {
        return new MutationContext(pi, documentName);
    }

    private static Document readDocument(final Path path) {
        final DocumentBuilder builder = ObjectFactory.createDocumentBuilder(false);
        try ( final InputStream input = Files.newInputStream(path) ) {
            return builder.parse(input);
        } catch (final SAXException | IOException e) {
            log.error("Error opening document {}", path, e);
            throw new IllegalArgumentException("Can not open Document " + path, e);
        }
    }
}
