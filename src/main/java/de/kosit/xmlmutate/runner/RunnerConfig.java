package de.kosit.xmlmutate.runner;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.validation.Schema;

import lombok.Getter;
import lombok.Setter;

import de.kosit.xmlmutate.mutation.NameGenerator;
import de.kosit.xmlmutate.mutation.Schematron;
import de.kosit.xmlmutate.mutation.SequenceNameGenerator;
import de.kosit.xmlmutate.report.ReportGenerator;
import de.kosit.xmlmutate.report.TextReportGenerator;
import de.kosit.xmlmutate.runner.MarkMutationAction.RemoveCommentAction;

/**
 * Die Arbeitsanweisung für den {@link MutationRunner}. Dieses Datenobjekt kapselt alle Informationen, damit der
 * {@link MutationRunner} die nötigen Arbeitsschritte auf den Zieldokumennten durchführen kann.
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

        public Builder withNameGenerator(final NameGenerator generator) {
            this.config.setNameGenerator(generator);
            return this;
        }

        public Builder withExecutor(final ExecutorService service) {
            this.config.setExecutorService(service);
            return this;

        }

        public RunnerConfig build() {
            this.config.getActions().add(new MarkMutationAction.InsertCommentAction());
            this.config.getActions().add(new MutateAction());
            this.config.getActions().add(new ValidateAction(this.config.getSchema(), this.config.getSchematronRules()));
            this.config.getActions().add(new CheckAction());
            this.config.getActions().add(new SerializeAction(this.config.getTargetFolder()));
            this.config.getActions().add(new ResetAction());
            this.config.getActions().add(new RemoveCommentAction());

            if (this.config.getExecutorService() == null) {
                this.config.setExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            }

            return this.config;
        }

        public Builder checkSchematron(final List<Schematron> rules) {
            this.config.setSchematronRules(rules);
            return this;
        }
    }

    /**
     * Zielverzeichnis für Ausgaben.
     */
    private Path targetFolder;

    private List<Path> documents;

    private Schema schema;

    private ReportGenerator reportGenerator = new TextReportGenerator(new PrintWriter(System.out));

    private NameGenerator nameGenerator = new SequenceNameGenerator();

    private List<RunAction> actions = new ArrayList<>();

    private List<Schematron> schematronRules = new ArrayList<>();

    private ExecutorService executorService;

}
