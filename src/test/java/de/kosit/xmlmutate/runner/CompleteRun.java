package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.TestResource.asPath;

import java.net.URI;
import java.util.concurrent.Executors;

import de.kosit.xmlmutate.TestResource;
import de.kosit.xmlmutate.TestResource.BookResources;

/**
 * This is a base class for tests running the complete process except the CLI-Parsing.
 * 
 * @author Andreas Penski
 */
public abstract class CompleteRun {

    protected RunnerResult run(final RunnerConfig config) {
        return new MutationRunner(config, Executors.newSingleThreadExecutor()).run();
    }

    protected RunnerConfig createConfig(final URI p) {
        return RunnerConfig.Builder.forDocuments(asPath(p)).checkSchema(BookResources.getSchema())
                .targetFolder(asPath(TestResource.TEST_TARGET)).build();
    }

    protected RunnerConfig createConfig(final URI document, final URI schema) {
        return RunnerConfig.Builder.forDocuments(asPath(document)).checkSchema(Services.getSchemaRepository().createSchema(schema))
                .targetFolder(asPath(TestResource.TEST_TARGET)).build();
    }
}
