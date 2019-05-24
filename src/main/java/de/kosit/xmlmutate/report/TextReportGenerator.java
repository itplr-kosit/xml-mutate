package de.kosit.xmlmutate.report;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;

/**
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class TextReportGenerator extends BaseReportGenerator {

    private final Writer writer;

    @Override
    public void generate(final List<Pair<Path, List<Mutation>>> results) {

        try {
            final List<Mutation> allMutations = results.stream().flatMap(p -> p.getValue().stream()).collect(Collectors.toList());
            this.writer.write(MessageFormat.format("Generated {0} valid and {1} invalid mutations from {2} files\n\n",
                    aggregateValid(allMutations), aggregateInvalid(allMutations), results.size()));
            for (final Pair<Path, List<Mutation>> p : results) {
                generate(p.getKey(), p.getValue());
                this.writer.write("\n");
            }
            this.writer.flush();
        } catch (final IOException e) {
            log.error("Error generating report", e);
        }
    }

    private void generate(final Path source, final List<Mutation> mutations) throws IOException {

        this.writer.write(MessageFormat.format("Generated {0} mutations from {1}\n", mutations.size(), source.toAbsolutePath()));

        this.writer.write(MessageFormat.format("Generated {0} valid mutations\n", aggregateValid(mutations)));
        this.writer.write(MessageFormat.format("Generated {0} invalid mutations\n", aggregateInvalid(mutations)));
        for (final Mutation mutation : mutations) {
            generateReport(mutation);
        }

    }

    private void generateReport(final Mutation mutation) throws IOException {
        if (mutation.getState() == State.ERROR) {
            this.writer.write(MessageFormat.format("Mutation {0} has processing errors: {1}\n", mutation.getIdentifier(),
                    mutation.getErrorMessage()));
        } else {
            this.writer.write(MessageFormat.format("Mutation {0} with mutator {1} was {2}: ", mutation.getIdentifier(),
                    mutation.getMutator().getName(), mutation.isValid() ? "successful" : "unsucessful"));
            if (mutation.isInvalid()) {
                this.writer.write("fehler ausgeben\n");
            } else {
                this.writer.write("\n");
            }
        }

    }

}
