package de.kosit.xmlmutate.report;

import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.runner.FailureMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fusesource.jansi.AnsiRenderer;
import org.fusesource.jansi.AnsiRenderer.Code;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A {@link ReportGenerator} that prints results to the console.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class TextReportGenerator extends BaseReportGenerator {

    /**
     * A definition / configuration for a column with a result table.
     */
    @Getter
    private static class ColumnDefinition {

        private static final int MAX_LENGTH = 80;

        private final String name;

        private int length;

        private final int definedLength;

        private final int maxLines;

        /**
         * Constructor.
         *
         * @param name the name of the column
         */
        public ColumnDefinition(final String name) {
            this(name, -1, 3);
        }

        /**
         * Constructor.
         *
         * @param name   the name of the column
         * @param length the max length of the column
         */
        public ColumnDefinition(final String name, final int length) {
            this(name, length, 3);
        }

        /**
         * Constructor.
         *
         * @param name     the name of the column
         * @param length   the max length of the column
         * @param maxLines the max lines per cell
         */
        public ColumnDefinition(final String name, final int length, final int maxLines) {
            this.name = name;
            this.definedLength = length;
            this.maxLines = maxLines;
        }

        /**
         * Returns the actual max length of the column
         *
         * @return max length
         */
        public int getLength() {
            return this.definedLength > 0 ? this.definedLength : this.length;
        }

        /**
         * Sets a calculated length for the column.
         *
         * @param length the length
         */
        public void setLength(final int length) {
            if (this.definedLength < 0 && getLength() < length) {
                this.length = length;
            }
            if (length > MAX_LENGTH) {
                this.length = MAX_LENGTH;
            }
        }

    }

    /**
     * A grid / table for printing results.
     */
    private static class Grid {

        private final List<ColumnDefinition> definitions = new ArrayList<>();

        private final List<Cell> values = new ArrayList<>();

        /**
         * Constructor.
         *
         * @param def {@link ColumnDefinition}s
         */
        public Grid(final ColumnDefinition... def) {
            Stream.of(def).forEach(this::addColumn);
        }

        private String generateGridStart() {
            return IntStream.range(0, getLineLength() + this.definitions.size()).mapToObj(i -> "_")
                    .collect(Collectors.joining("")) + "\n";
        }

        private String generateGridEnd() {
            return IntStream.range(0, getLineLength() + this.definitions.size()).mapToObj(i -> "_")
                    .collect(Collectors.joining("")) + "\n";
        }

        private String generateHeader() {
            return "|" + this.definitions.stream().map(d -> StringUtils.rightPad(d.getName(), d.getLength()))
                    .collect(Collectors.joining("|")) + "|\n";
        }

        /**
         * Adds new a column definition.
         *
         * @param def definitions
         * @return this grid
         */
        public Grid addColumn(final ColumnDefinition def) {
            this.definitions.add(def);
            return this;
        }

        private void calculateLength() {
            IntStream.range(0, this.definitions.size()).forEach(i -> {
                final ColumnDefinition def = this.definitions.get(i);
                final List<Cell> column = getColumn(i);
                final int maxLength = column.stream().mapToInt(cell -> {
                    return cell.getText().stream().mapToInt(Text::getLength).sum();
                }).max().orElse(0);

                def.setLength(Math.max(maxLength, def.getName().length()));

            });
        }

        public List<Cell> getColumn(final int index) {

            return IntStream.range(0, this.values.size()).filter(n -> n % this.definitions.size() == index)
                    .mapToObj(this.values::get).collect(Collectors.toList());
        }

        public Grid addCell(final Cell cell) {
            this.values.add(cell);
            return this;
        }

        public Grid addCell(final Text... text) {
            return addCell(new Cell(Arrays.asList(text)));
        }

        public Grid addCell(final Object cell, final Code... codes) {
            final Format f = new Format();
            f.addCodes(codes);
            final Text t = new Text(cell, f);
            return addCell(new Cell(t));
        }

        public Grid addCell(final Object cell) {
            return addCell(cell, DEFAULT_FORMAT.textColor);
        }

        private Collection<List<Cell>> prepareLines() {
            final AtomicInteger counter = new AtomicInteger();
            final int chunkSize = this.definitions.size();
            return this.values.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                    .values();
        }

        public String print() {
            final StringBuilder b = new StringBuilder();
            calculateLength();
            b.append(generateGridStart());
            b.append(generateHeader());
            prepareLines().forEach(line -> b.append(printLine(line)));

            b.append(generateGridEnd());
            return b.toString();
        }

        private String printLine(final List<Cell> line) {
            final StringBuilder b = new StringBuilder();
            int virtualLine = 0;
            while (true) {
                final StringBuilder current = new StringBuilder();
                final int bound = this.definitions.size();
                for (int i = 0; i < bound; i++) {
                    final ColumnDefinition def = this.definitions.get(i);
                    current.append("|");
                    current.append(line.get(i).toString(virtualLine, def));
                }
                current.append("|");
                if (isEmpty(current) || virtualLine >= getMaxVirtualLine()) {
                    break;
                }
                b.append(current.toString());
                virtualLine++;
                b.append("\n");
            }
            return b.toString();

        }

        private boolean isEmpty(final StringBuilder current) {
            return current.toString().replaceAll("\\|", "").trim().length() == 0;
        }

        private int getMaxVirtualLine() {
            return this.definitions.stream().mapToInt(ColumnDefinition::getMaxLines).max()
                    .orElseThrow(IllegalAccessError::new);
        }

        private int getLineLength() {
            return this.definitions.stream().map(ColumnDefinition::getLength).reduce(0, Integer::sum);
        }

    }

    @RequiredArgsConstructor
    @Getter
    private static class Cell {

        private final Format format = DEFAULT_FORMAT;

        private final List<Text> text;

        public Cell(final Text txt) {
            this.text = new ArrayList<>();
            this.text.add(txt);
        }

        public Cell(final Object object, final Code... codes) {
            this(new Text(object, codes));
        }

        public String toString(final int row, final ColumnDefinition def) {
            final StringBuilder b = new StringBuilder();
            int startSubstring = row * def.getLength();
            int visibleLength = 0;
            if (startSubstring >= 0) {
                for (final Text t : this.text) {
                    String part = t.getVisibleText(startSubstring, def.getLength() - visibleLength);
                    if (b.length() + part.length() >= def.getLength() && def.getMaxLines() == row + 1) {
                        part = dotted(part);
                    }
                    visibleLength += part.length();
                    if (StringUtils.isNotBlank(part)) {
                        b.append(t.render(part, DEFAULT_FORMAT));
                        if (visibleLength >= def.getLength()) {
                            break;
                        }
                        startSubstring = 0;
                    } else {
                        startSubstring = startSubstring - t.getLength();
                    }
                }
            }
            final String target = b.toString();
            return StringUtils
                    .rightPad(target, def.getLength() + (target.length() > 0 ? target.length() - visibleLength : 0));
        }

        public Cell add(final Object object, final Code... codes) {
            this.text.add(new Text(object, codes));
            return this;
        }

        private String dotted(final String part) {
            return part.substring(0, part.length() - 3) + "...";
        }

    }

    @Getter
    private static class Text {

        private final String text;

        private Format format;

        public Text(final Object text) {
            this.text = text != null ? text.toString() : "";
            this.format = DEFAULT_FORMAT;
        }

        public Text(final Object text, final Format format) {
            this(text);
            this.format = format;
        }

        public Text(final Object text, final Code... codes) {
            this(text, new Format().addCodes(codes));
        }

        private String getVisibleText(final int startIndex, final int length) {
            if (startIndex < 0) {
                return "Wrong cell text index";
            }
            if (startIndex > this.text.length()) {
                return "";
            }
            final String substring = this.text.substring(startIndex);
            return substring.length() > length ? substring.substring(0, length) : substring;
        }

        private String render(final String text, final Format baseformat) {
            return AnsiRenderer.render(
                    text, Arrays.stream(this.format.mergeCodes(baseformat.getCodes())).map(Code::name)
                            .toArray(String[]::new));
        }

        public int getLength() {
            return this.text.length();
        }

        public String render(final Format baseFormat) {
            return render(getText(), baseFormat);
        }
    }

    private static class Format {

        private Code textColor;

        private Code background;

        @Getter
        private final Set<Code> codes = new HashSet<>();

        private Code[] mergeCodes(final Collection<Code> newCodes) {
            return mergeCodes(newCodes.toArray(new Code[newCodes.size()]));
        }

        private Code[] mergeCodes(final Code... newCodes) {
            final Code[] allCodes = ArrayUtils.addAll(
                    ArrayUtils.addAll(this.codes.toArray(new Code[0]), newCodes), this.textColor, this.background);

            final Optional<Code> color = Arrays.stream(allCodes).filter(Objects::nonNull).filter(Code::isColor)
                    .findFirst();
            final Optional<Code> bg = Arrays.stream(allCodes).filter(Objects::nonNull).filter(Code::isBackground)
                    .findFirst();
            final List<Code> attributes = Arrays.stream(allCodes).filter(Objects::nonNull).filter(Code::isBackground)
                    .filter(Code::isColor).collect(Collectors.toList());
            attributes.add(color.orElse(this.textColor));
            attributes.add(bg.orElse(this.background));
            return attributes.stream().filter(Objects::nonNull).toArray(Code[]::new);
        }

        /**
         * Sets explicit text color.
         *
         * @param textColor the color.
         * @return this {@link Format}
         */
        public Format color(final Code textColor) {
            this.textColor = textColor;
            return this;
        }

        /**
         * Sets explicit background color.
         *
         * @param color the color.
         * @return this {@link Format}
         */
        public Format background(final Code color) {
            this.background = color;
            return this;
        }

        /**
         * Fügt weitere Formatierungscodes hinzu.
         *
         * @param codes die Codes
         * @return this {@link Format}
         */
        public Format addCodes(final Code... codes) {
            this.codes.addAll(Arrays.asList(codes));
            return this;
        }
    }

    /**
     * Helper for printing a colored line (with newline at the end) to the console.
     */
    @NoArgsConstructor
    private static class Line {

        private final List<Text> texts = new ArrayList<>();

        private Format baseFormat = DEFAULT_FORMAT;

        /**
         * Constructor.
         *
         * @param format the configured base format
         */
        public Line(final Format format) {
            this.baseFormat = format;
        }

        /**
         * Constructor.
         *
         * @param codes Ansi escape codes for formatting
         */
        public Line(final Code... codes) {
            this(new Format().addCodes(codes));
        }

        /**
         * Add some text to the line.
         *
         * @param text the text
         * @return this line
         */
        public Line add(final Text text) {
            this.texts.add(text);
            return this;
        }

        public Line add(final Object t) {
            return add(new Text(t));
        }

        public Line add(final Object text, final Code... codes) {
            return add(new Text(text, codes));
        }

        public String render() {
            final String blank = AnsiRenderer
                    .render(" ", Arrays.stream(this.baseFormat.mergeCodes()).map(Code::name).toArray(String[]::new));
            return this.texts.stream().map(t -> t.render(this.baseFormat)).collect(Collectors.joining(blank)) + "\n";

        }

        public int getLength() {
            return this.texts.stream().mapToInt(Text::getLength).sum() + this.texts.size();
        }
    }

    private static final Format DEFAULT_FORMAT = new Format();

    private static final Cell EMPTY = new Cell(new Text(" "));

    private final Writer writer;

    @Override
    public void generate(final List<Pair<Path, List<Mutation>>> results, final FailureMode failureMode) {
        try {
            // report on each mutation
            for (final Pair<Path, List<Mutation>> p : results) {
                generateMutationReportTable(p.getKey(), p.getValue());
                this.writer.write("\n");
            }
            // Final result lines
            final List<Mutation> allMutations = results.stream().flatMap(p -> p.getValue().stream())
                    .collect(Collectors.toList());
            final Line summary = new Line(Code.BOLD).add("Generated").add(allMutations.size(), Code.YELLOW)
                    .add("mutations. Passed:").add(countSuccessful(allMutations), Code.GREEN).add(". Failed:")
                    .add(countFailures(allMutations), Code.RED).add(". Error:").add(countErrors(allMutations), Code.RED);

            final String dashes = StringUtils.rightPad("", summary.getLength(), "-");
            // now writing
            this.writer.write(dashes + "\n");
            final boolean sucess = countSuccessful(allMutations) == allMutations.size();
            final boolean failureButSuccess = failureMode == FailureMode.FAIL_NEVER;
            this.writer.write(
                    new Line((sucess || failureButSuccess ? Code.GREEN : Code.RED)).add("Result: " + (sucess || failureButSuccess ? "SUCCESSFUL" : "FAILURE"))
                            .render());
            this.writer.write(summary.render());
            this.writer.write(dashes + "\n");
            this.writer.write(new Line(Code.BLUE).add(getVersionLine()).render());
            this.writer.write(dashes);
            this.writer.flush();
        } catch (final IOException e) {
            log.error("Error generating report", e);
        }
    }

    private void generateMutationReportTable(final Path source, final List<Mutation> mutations) throws IOException {
        final Line header = new Line(Code.BG_GREEN);

        header.add("Generated").add(mutations.size()).add("mutations from").add(source.toString(), Code.BLUE);
        this.writer.write(header.render());
        if (mutations.isEmpty()) {
            final Line noMutationError = new Line(Code.RED);
            noMutationError.add("No mutations found within this file");
            this.writer.write(noMutationError.render());
        } else {

            final Grid grid = new Grid(new ColumnDefinition("#", 3), new ColumnDefinition("Mutation", 15),
                    new ColumnDefinition("Line", 4), new ColumnDefinition("Exp"), new ColumnDefinition("XSD", 3),
                    new ColumnDefinition("Exp", 3), new ColumnDefinition("Sch", 3),
                    new ColumnDefinition("Exp", 15, 1000), new ColumnDefinition("Error Message", 50),
                    new ColumnDefinition("Description", 15, 10));

            IntStream.range(0, mutations.size()).forEach(i -> {
                this.generateMutationReportLine(grid, mutations.get(i), i);
            });
            this.writer.write(grid.print());
            final long failureCount = countFailures(mutations);
            final int errorCount = countErrors(mutations);
            final Line summary = new Line(failureCount + errorCount == 0 ? Code.GREEN : Code.RED);
            summary.add("Mutations run:").add(mutations.size()).add(". Failures:").add(failureCount).add(". Errors:")
                    .add(errorCount);
            this.writer.write(summary.render());

        }
    }

    private void generateMutationReportLine(final Grid grid, final Mutation mutation, final int mutationNum) {

        final boolean isSchemaValid = mutation.isSchemaValid();
        final boolean isSchemaProcessed = mutation.isSchemaProcessed();
        final boolean isSchemaExpectationSet = mutation.isSchemaExpectationSet();
        final boolean asSchemaExpected = mutation.isSchemaValidationAsExpected();
        final boolean isSchematronValid = mutation.isSchematronValid();
        final boolean isSchematronProcessed = mutation.isSchematronProcessed();
        final boolean isSchematronExpectationSet = mutation.isSchematronExpectationSet();

        final List<SchematronRuleExpectation> failed = mutation.getResult().getSchematronExpectationMatches().entrySet()
                .stream().filter(e -> Boolean.FALSE.equals(e.getValue())).map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Create common error message list
        final List<String> allErrors = mutation.getMutationErrorContainer().getAllErrorMessagesSorted(failed.stream()
                .map(SchematronRuleExpectation::getRuleName).collect(Collectors.toList()));

        final List<Cell> expectationCells = createSchematronExpectationCells(isSchematronProcessed, isSchematronExpectationSet, failed);


        // grid.addCell(mutation.getIdentifier());
        grid.addCell(Integer.toString(mutationNum + 1));
        grid.addCell(mutation.getMutator() != null ? mutation.getMutator().getNames() + " " + mutation.getIdentifier() : "");
        grid.addCell(mutation.getContext().getLineNumber());
        grid.addCell(createOverallResult(mutation));
        grid.addCell(createSchemaValidationCell(isSchemaProcessed, isSchemaValid));
        grid.addCell(createSchemaExpectationCell(isSchemaProcessed, isSchemaExpectationSet, asSchemaExpected));
        grid.addCell(createSchematronValidationCell(isSchematronProcessed, isSchematronValid));
        grid.addCell(expectationCells.get(0));

        // Create first error message
        if (allErrors.isEmpty()) {
            grid.addCell("");
        } else {
            grid.addCell(allErrors.get(0));
        }

        final Object description = mutation.getConfiguration().getProperties().get("description");
        if (description != null) {
            grid.addCell(description);
        } else {
            grid.addCell(EMPTY);
        }

        // Rest of error messages and failed schematron rules (if they exist)
        for (int i = 1; i < allErrors.size(); i++) {
            grid.addCell(EMPTY);
            grid.addCell(EMPTY);
            grid.addCell(EMPTY);
            grid.addCell(EMPTY);
            grid.addCell(EMPTY);
            grid.addCell(EMPTY);
            grid.addCell(EMPTY);
            if (expectationCells.size() > i) {
                grid.addCell(expectationCells.get(i));
            } else {
                grid.addCell(EMPTY);
            }
            grid.addCell(allErrors.get(i));
            grid.addCell(EMPTY);
        }

    }


    private Cell createOverallResult(final Mutation mutation) {
        final Cell overall;
        if (mutation.isAllAsExpected() || mutation.isOneAsExpectedAndOneUnprocessed()) {
            overall = new Cell("Y", Code.GREEN);
        } else if (mutation.isAllUnprocessed()) {
            overall = new Cell("NA", Code.CYAN);
        } else {
            overall = new Cell("N", Code.RED);
        }
        return overall;
    }

    private Cell createSchematronValidationCell(final boolean isProcessed, final boolean isValid) {

        if (!isProcessed) {
            return new Cell("NA", Code.CYAN);
        }

        if (isValid) {
            return new Cell("Y", Code.GREEN);
        }
        return new Cell("N", Code.RED);
    }

    private List<Cell> createSchematronExpectationCells(final boolean isProcessed, final boolean isSchematronExpectationSet, final List<SchematronRuleExpectation> failed) {

        final List<Cell> cells = new ArrayList<>();

        if (!isProcessed || !isSchematronExpectationSet) {
            cells.add(new Cell("NA", Code.CYAN));
            return cells;
        }

        if (failed.isEmpty()) {
            cells.add(new Cell("Y", Code.GREEN));
            return cells;
        }
        failed.forEach(e -> cells.add(new Cell(e.getRuleName() + ":N", Code.RED)));
        return cells;
    }

    private Cell createSchemaValidationCell(final boolean isProcessed, final boolean isValid) {
        if (!isProcessed) {
            return new Cell("NA", Code.CYAN);
        }

        return new Cell(isValid ? "Y" : "N", isValid ? Code.GREEN : Code.RED);
    }

    private Cell createSchemaExpectationCell(final boolean isProcessed, final boolean isSchemaExpectationSet, final boolean asExpected) {
        if (!isProcessed || !isSchemaExpectationSet) {
            return new Cell("NA", Code.CYAN);
        }
        return new Cell(asExpected ? "Y" : "N", asExpected ? Code.GREEN : Code.RED);
    }
}
