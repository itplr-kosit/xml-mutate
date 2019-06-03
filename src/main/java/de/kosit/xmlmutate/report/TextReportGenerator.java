package de.kosit.xmlmutate.report;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.fusesource.jansi.AnsiRenderer;
import org.fusesource.jansi.AnsiRenderer.Code;

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

    private static class Format {

        private Code textColor;

        private Code background;

        private final int minLength = -1;

        private final Set<Code> codes = new HashSet<>();

        private StringBuilder builder = new StringBuilder();

        private Code[] mergeCodes(final Code... newCodes) {
            final Optional<Code> color = Arrays.stream(newCodes).filter(Code::isColor).findFirst();
            final Optional<Code> bg = Arrays.stream(newCodes).filter(Code::isBackground).findFirst();
            final List<Code> attributes = Arrays.stream(newCodes).filter(Code::isBackground).filter(Code::isColor)
                    .collect(Collectors.toList());
            attributes.add(color.orElse(this.textColor));
            attributes.add(bg.orElse(this.background));
            return attributes.stream().filter(e -> e != null).toArray(Code[]::new);
        }

        public Format color(final Code textColor) {
            this.textColor = textColor;
            return this;
        }

        public Format background(final Code bg) {
            this.background = bg;
            return this;
        }

        public Format append(final Object data, final Code... codes) {
            this.builder
                    .append(AnsiRenderer.render(data.toString(), Arrays.stream(mergeCodes(codes)).map(Code::name).toArray(String[]::new)));
            return this;
        }

        public Format append(final Object data, final int minLength) {
            return append(data, minLength, new Code[0]);
        }

        public Format append(final Object data, final int minLength, final Code... codes) {
            append(data, codes);
            final int fillLength = minLength - data.toString().length();
            fill(" ", fillLength);
            return this;
        }

        private void fill(final String s, final int fillLength) {
            IntStream.range(0, fillLength).forEach(e -> append(s));
        }

        public Format append(final Object data) {
            return append(data, new Code[0]);
        }

        public void write(final Writer writer) throws IOException {
            writer.write(toString());
            this.builder = new StringBuilder();
        }

        public void writeln(final Writer writer) throws IOException {
            writer.write(toString() + "\n");
            this.builder = new StringBuilder();
        }

        @Override
        public String toString() {
            return this.builder.toString();
        }

        public Format repeat(final String str, final int count) {
            fill(str, count);
            return this;
        }
    }

    private final Writer writer;

    public static Format format() {
        return new Format();
    }

    @Override
    public void generate(final List<Pair<Path, List<Mutation>>> results) {
        try {
            final List<Mutation> allMutations = results.stream().flatMap(p -> p.getValue().stream()).collect(Collectors.toList());
            format().background(Code.BG_MAGENTA).append("Summary: Generated ").append(aggregateValid(allMutations), Code.GREEN)
                    .append(" " + "mutations" + " " + "and ").append(aggregateInvalid(allMutations), Code.RED)
                    .append(" invalid mutations from ").writeln(this.writer);

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
        format().background(Code.BG_CYAN).append("Generated ").append(mutations.size()).append(" mutations from ")
                .append(source.toString(), Code.BLUE).writeln(this.writer);
        for (final Mutation mutation : mutations) {
            generateReport(mutation);
        }
    }

    private void generateReport(final Mutation mutation) throws IOException {

        final Format format = format();
        format.append("Line " + mutation.getContext().getLineNumber(), 10).write(this.writer);
        format.append("|" + mutation.getIdentifier(), 20);

        if (mutation.getState() == State.ERROR) {
            format.append("|" + mutation.getState(), 7, Code.RED);
            format.append("|" + mutation.getErrorMessage(), 40);
        } else if (mutation.isValid()) {
            format.append("|" + "OK", 7, Code.GREEN);
            format.append("|" + " ", 40);
        } else {
            format.append("|FAILED", 7, Code.YELLOW);
            format.append("|" + mutation.getErrorMessage(), 40, Code.YELLOW);
        }
        format.append("|").writeln(this.writer);

    }


}
