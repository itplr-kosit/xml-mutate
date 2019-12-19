package de.kosit.xmlmutate.runner;

import static de.kosit.xmlmutate.TestResource.asPath;

import java.net.URI;

import de.kosit.xmlmutate.TestResource;
import de.kosit.xmlmutate.TestResource.BookResources;

/**
 * This is a base class for tests running the complete process except the CLI-Parsing.
 * 
 * @author Andreas Penski
 */
public abstract class CompleteRun {

    protected RunnerConfig createConfig(final URI p) {
        return RunnerConfig.Builder.forDocuments(asPath(p)).checkSchema(BookResources.getSchema())
                .targetFolder(asPath(TestResource.TEST_TARGET)).build();
    }
}
