package de.kosit.xmlmutate.mutation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;

import lombok.extern.slf4j.Slf4j;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.Result;
import de.init.kosit.commons.SyntaxError;
import de.init.kosit.commons.validate.SchemaValidationService;
import de.kosit.xmlmutate.Services;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult.ValidationState;
import de.kosit.xmlmutate.mutator.Mutator;

/**
 * @author Andreas Penski
 */
@Slf4j
public class MutationRunner {

    private final RunnerConfig configuration;

    private final MutationParser parser;

    private final SchemaValidationService validationService = new SchemaValidationService();

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
            return new ImmutablePair(path, processDocument(d, path.getFileName().toString()));
        });

    }

    private void process(final Mutation mutation) {
        if (mutation.getState() != State.ERROR) {
            log.info("Running mutation {}", mutation.getIdentifier());
            mutate(mutation);
            validate(mutation);
            check(mutation);
            serialize(mutation);
            resetDocument(mutation);
        }
    }

    static void check(final Mutation mutation) {
        mutation.getConfiguration().getSchematronExpectations().forEach(e -> {
            mutation.getResult().getExpectationResult().put(e, check(e, mutation.getResult()));
        });
        mutation.setState(State.CHECKED);
    }

    private static boolean check(final Expectation e, final MutationResult result) {
        final Collection<SchematronOutput> targets;
        if (e.getSchematronSource() != null) {
            final Optional<SchematronOutput> schematronResult = result.getSchematronResult(e.getSchematronSource());
            targets = schematronResult.map(Collections::singletonList).orElseGet(ArrayList::new);
        } else {
            targets = result.getSchematronResult().values();
        }

        // TODO prüfen ob das auch bei keinem Schematron match stimmt
        final Optional<FailedAssert> failed = targets.stream().map(SchematronOutput::getFailedAsserts).flatMap(List::stream)
                .filter(f -> f.getId().equals(e.ruleName())).findAny();
        return (failed.isPresent() && e.mustFail()) || (!failed.isPresent() && !e.mustFail());
    }

    private void resetDocument(final Mutation mutation) {
        final MutationContext context = mutation.getContext();
        final Element parent = context.getParentElement();
        parent.replaceChild(context.getOriginalFragment(), context.getTarget());
        uncommentMutation(mutation);
    }

    private void mutate(final Mutation mutation) {
        log.info("Running mutation {} on element {}", mutation.getMutator().getName(), mutation.getContext().getTarget().getNodeName());
        final Mutator mutator = mutation.getMutator();
        commentMutation(mutation);
        mutator.mutate(mutation.getContext(), mutation.getConfiguration());
        mutation.setState(State.MUTATED);
    }

    private static void commentMutation(final Mutation mutation) {
        final Comment comment = mutation.getContext().getDocument()
                .createComment(MessageFormat.format(" This is the active mutation configuration: {0} ", mutation.getIdentifier()));
        final Text textNode = mutation.getContext().getDocument().createTextNode("\n");
        mutation.getContext().getParentElement().insertBefore(comment, mutation.getContext().getTarget());
        mutation.getContext().getParentElement().insertBefore(textNode, mutation.getContext().getTarget());
    }

    private static void uncommentMutation(final Mutation mutation) {
        final Element parent = mutation.getContext().getParentElement();
        final Node lf = mutation.getContext().getPi().getNextSibling();
        parent.removeChild(lf);
        final Node comment = mutation.getContext().getPi().getNextSibling();
        parent.removeChild(comment);
    }

    private void validate(final Mutation mutation) {
        log.info("validating");
        schemaValidation(mutation);
        schematronValidation(mutation);
        mutation.setState(State.VALIDATED);
    }

    private void schematronValidation(final Mutation mutation) {
        long failedAssertCount = 0;

        for (final Schematron s : this.configuration.getSchematronRules()) {
            final SchematronOutput out = Services.schematronService.validate(s.getUri(), mutation.getContext().getDocument());
            failedAssertCount += out.getFailedAsserts().size();
            mutation.getResult().addSchematronResult(s, out);
        }
        mutation.getResult().setSchematronValidation(failedAssertCount > 0 ? ValidationState.INVALID : ValidationState.VALID);
    }

    private void schemaValidation(final Mutation mutation) {
        final Schema schema = this.configuration.getSchema();
        if (schema != null) {
            final Result<Boolean, SyntaxError> result = this.validationService.validate(schema, mutation.getContext().getDocument());
            mutation.getResult().getSchemaValidationErrors().addAll(result.getErrors());
            mutation.getResult().setSchemaValidation(result.isValid() ? ValidationState.VALID : ValidationState.INVALID);
        }

    }

    private void serialize(final Mutation mutation) {
        try {
            final Path target = this.configuration.getTargetFolder().resolve(mutation.getResultDocument());
            Files.createDirectories(target.getParent());
            final OutputStream out = Files.newOutputStream(target);
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(new DOMSource(mutation.getContext().getDocument()),
                    new StreamResult(new OutputStreamWriter(out, Charsets.UTF_8)));
            out.close();
        } catch (final TransformerException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Mutation> processDocument(final Document origin, final String documentName) {
        final List<Mutation> all = new ArrayList<>();
        final TreeWalker piWalker = ((DocumentTraversal) origin).createTreeWalker(origin, NodeFilter.SHOW_PROCESSING_INSTRUCTION, null,
                true);
        while (piWalker.nextNode() != null) {
            final ProcessingInstruction pi = (ProcessingInstruction) piWalker.getCurrentNode();
            final MutationContext context = createContext(documentName, pi);
            final List<Mutation> mutations = this.parser.parse(context);
            mutations.forEach(this::process);
            all.addAll(mutations);
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
