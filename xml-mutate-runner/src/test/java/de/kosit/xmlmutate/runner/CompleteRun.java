package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.TestResource.asPath;

import java.net.URI;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;

import de.kosit.xmlmutate.RunnerTestHelper;
import de.kosit.xmlmutate.TestResource;

/**
 * This is a base class for tests running the complete process except the CLI-Parsing.
 * 
 * @author Andreas Penski
 */
public abstract class CompleteRun {

    @BeforeEach
    public void cleanup() {
        Services.getTemplateRepository().clear();
    }

    protected RunnerResult run(final RunnerConfig config) {
        return new MutationRunner(config, Executors.newSingleThreadExecutor()).run();
    }

    protected RunnerConfig createConfig(final URI p) {
        return RunnerConfig.Builder.forDocuments(asPath(p)).checkSchema(RunnerTestHelper.getBookSchema())
                .targetFolder(asPath(TestResource.TEST_TARGET)).build();
    }

    protected RunnerConfig createConfig(final URI document, final URI schema) {
        return RunnerConfig.Builder.forDocuments(asPath(document)).checkSchema(ValidatorServices.getSchemaRepository().createSchema(schema))
                .targetFolder(asPath(TestResource.TEST_TARGET)).build();
    }
}
