package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.runner.MutationProcessor.serialize;
import static de.kosit.xmlmutate.runner.MutationProcessor.mutateDocument;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.MutationContext;
import de.kosit.xmlmutate.parser.MutatorInstruction;

/**
 * Runner coordinates whole process of Mutation and Testing.
 *
 * @author Renzo Kottmann
 * @author Andreas Penski
 */
public class MutationRunner {
    private static final Logger log = LoggerFactory.getLogger(MutationRunner.class);
    private final RunnerConfig configuration;

    /**
     * xmute Processing Instruction parser
     */

    private final ExecutorService executorService;

    private final TemplateRepository templateRepository;

    public MutationRunner(final RunnerConfig configuration, final ExecutorService executorService) {
        this.configuration = configuration;

        this.executorService = executorService;
        this.templateRepository = Services.getTemplateRepository();
    }

    public void run() {
        prepare();
        // executorService.
        // final RunnerDocumentContext results =
        // this.configuration.getDocuments().stream().map(this::process)
        // .collect(Collectors.toList());
        this.configuration.getDocuments().forEach(d -> {
            process(d);
        });
        // map(this::process);
        // .map(MutationRunner::awaitTermination);
        // this.configuration.getReportGenerator().generate(results);

    }

    private void prepare() {
        // register templates
        this.configuration.getTemplates()
                .forEach(t -> this.templateRepository.registerTemplate(t.getName(), t.getPath()));
    }

    /**
     * Processing a Document at given Path
     *
     * @param path
     *                 to the Document
     * @return
     */
    private RunnerDocumentContext process(final Path path) {

        final Document d = DocumentParser.readDocument(path);
        final List<MutatorInstruction> instructions = DocumentParser
                .parseMutatorInstruction(d, path.getFileName().toString());
        // Processing erfolgt sortiert nach nesting tiefe (von tief nach hoch)
        // Grund hierfür ist, das durch Entfernen von Knoten möglicherweise PI aus dem
        // Kontext gerissen werden.
        final List<MutatorInstruction> sorted = instructions.stream()
                .sorted(Comparator.comparing(e -> e.getLevel(), Comparator.reverseOrder()))
                .collect(Collectors.toList());
        RunnerDocumentContext context = new RunnerDocumentContext(d, sorted);
        processInstructions(context);
        return context;

    }

    /**
     * Processing Actions for MutatorInstructions of an Document
     */
    private void process(final RunnerDocumentContext context) {
        log.debug("Running MutatorInstruction ");

        this.configuration.getActions().forEach(
                a -> {

                    // log.debug("Running {} for {}", a.getClass().getSimpleName(),
                    // mutation.getIdentifier());
                    a.run(context);

                });
    }

    private void processInstructions(final RunnerDocumentContext context) {
        final Document d = context.getOriginalDocument();
        final Path targetFolder = configuration.getTargetFolder();
        context.getInstructions().forEach(
                i -> i.createMutants().forEach(

                        m -> {
                            // apply mutation to original doc
                            mutateDocument(d, m, false);

                            serialize(d, m, targetFolder);

                            validateSchema(d, m, configuration.getSchema());
                        }
                // validate against schema
                // 3. validated against schematron(s)
                // 4. evaluated against expectations

                ));
    }

    private List<Mutation> parseMutations(final Document origin, final String documentName) {
        final List<Mutation> all = new ArrayList<>();
        final TreeWalker piWalker = ((DocumentTraversal) origin)
                .createTreeWalker(origin, NodeFilter.SHOW_PROCESSING_INSTRUCTION, null, true);
        while (piWalker.nextNode() != null) {
            final ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
            if (pi.getTarget().equals("xmute")) {
                final MutationContext context = new MutationContext(pi, documentName);
                // final List<Mutation> mutations = this.parser.parse(context);
                // all.addAll(mutations);
            }
        }
        return all;
    }

}
