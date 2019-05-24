package de.kosit.xmlmutate.runner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public MutationRunner(final RunnerConfig configuration) {
        this.configuration = configuration;
        this.parser = new MutationParser(configuration.getNameGenerator());
    }

    public void run() {
        final List<Pair<Path, List<Mutation>>> results = this.configuration.getDocuments().stream().map(this::process)
                .map(MutationRunner::awaitTermination).collect(Collectors.toList());
        this.configuration.getReportGenerator().generate(results);
        this.executorService.shutdown();

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
            return new ImmutablePair<>(path, processDocument(d, path.getFileName().toString()));
        });

    }

    private void process(final Mutation mutation) {
        if (mutation.getState() != State.ERROR) {
            log.info("Running mutation {}", mutation.getIdentifier());
            this.configuration.getActions().forEach(a -> a.run(mutation));
        }
    }

    private List<Mutation> processDocument(final Document origin, final String documentName) {
        final List<Mutation> all = new ArrayList<>();
        final TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin, NodeFilter.SHOW_PROCESSING_INSTRUCTION, null,
                true);
        while (piWalker.nextNode() != null) {
            final ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
            if (pi.getTarget().equals("xmute")) {
                final MutationContext context = createContext(documentName, pi);
                final List<Mutation> mutations = this.parser.parse(context);
                mutations.forEach(this::process);
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
