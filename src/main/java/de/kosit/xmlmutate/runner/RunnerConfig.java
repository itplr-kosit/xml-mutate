package de.kosit.xmlmutate.runner;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.validation.Schema;

import lombok.Getter;
import lombok.Setter;

import de.kosit.xmlmutate.mutation.NamedTemplate;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.report.ReportGenerator;
import de.kosit.xmlmutate.report.TextReportGenerator;
import de.kosit.xmlmutate.runner.MarkMutationAction.RemoveCommentAction;

/**
 * Contains whole configuration for a {@link MutationRunner} including all Actions.
 *
 * @author Andreas Penski
 */
@Getter
@Setter
public class RunnerConfig {

    public static class Builder {

        private final RunnerConfig config = new RunnerConfig();

        public RunMode mode = RunMode.ALL;

        public static Builder forDocuments(final List<Path> docs) {
            final Builder b = new Builder();
            b.config.setDocuments(docs);
            return b;
        }

        public static Builder forDocuments(final Path doc) {
            return forDocuments(Collections.singletonList(doc));
        }

        public Builder mode(final RunMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder targetFolder(final Path p) {
            this.config.setTargetFolder(p);
            return this;
        }

        public Builder checkSchema(final Schema schema) {
            this.config.setSchema(schema);
            return this;
        }

        public Builder reportWith(final ReportGenerator generator) {
            this.config.setReportGenerator(generator);
            return this;
        }

        public Builder withExecutor(final ExecutorService service) {
            this.config.setExecutorService(service);
            return this;
        }

        public Builder checkSchematron(final List<Schematron> rules) {
            this.config.setSchematronRules(rules);
            return this;
        }

        public Builder useTransformations(final List<NamedTemplate> templates) {
            this.config.setTemplates(templates);
            return this;
        }

        public Builder withFailureMode(final FailureMode failureMode) {
            this.config.setFailureMode(failureMode);
            return this;
        }

        public Builder withSavingMode(final SavingMode savingMode) {
            this.config.setSavingMode(savingMode);
            return this;
        }

        public Builder withIgnoreSchemaInvalidity(final boolean ignoreSchemaInvalidity) {
            this.config.setIgnoreSchemaInvalidity(ignoreSchemaInvalidity);
            return this;
        }

        public Builder saveParsing(boolean saveParsingMode) {
            this.config.setSaveParsing(saveParsingMode);
            return this;
        }

        public RunnerConfig build() {
            this.config.getActions().add(new MarkMutationAction.InsertCommentAction());
            this.config.getActions().add(new MutateAction());
            this.config.getActions().add(new SerializeAction(this.config.getTargetFolder()));
            this.config.getActions()
                    .add(new ValidateAction(this.config.getSchema(), this.config.getSchematronRules(), this.config.getTargetFolder()));
            this.config.getActions().add(new EvaluateSchematronExpectationsAction());
            this.config.getActions().add(new ResetAction());
            this.config.getActions().add(new RemoveCommentAction());

            if (this.config.getExecutorService() == null) {
                this.config.setExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            }
            return this.config;
        }

    }

    /**
     * Target directory for outputs
     */
    private Path targetFolder;

    private List<Path> documents;

    private Schema schema;

    private ReportGenerator reportGenerator = new TextReportGenerator(new PrintWriter(System.out));

    private List<RunAction> actions = new ArrayList<>();

    private List<Schematron> schematronRules = new ArrayList<>();

    private List<NamedTemplate> templates;

    private ExecutorService executorService;

    private FailureMode failureMode;

    private SavingMode savingMode = SavingMode.SINGLE;

    private boolean saveParsing;

    private boolean ignoreSchemaInvalidity;

    public void addTemplate(final String name, final Path transform) {
        if (this.templates == null) {
            this.templates = new ArrayList<>();
        }
        this.templates.add(new NamedTemplate(name, transform.toUri()));
    }
}
