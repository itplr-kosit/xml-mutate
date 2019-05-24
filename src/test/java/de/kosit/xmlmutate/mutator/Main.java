package de.kosit.xmlmutate.mutator;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import de.init.kosit.commons.validate.SchemaRepository;
import de.kosit.xmlmutate.mutation.MutationRunner;
import de.kosit.xmlmutate.mutation.RunnerConfig;
import de.kosit.xmlmutate.report.TextReportGenerator;

/**
 * @author Andreas Penski
 */
public class Main {

    private static final SchemaRepository repository = new SchemaRepository();

    @Test
    public void testSimple() throws URISyntaxException {
        final URL target = Main.class.getClassLoader().getResource("ubl-invoice-remove-mutation-tests.xml");
        final RunnerConfig c = new RunnerConfig();
        c.setDocuments(Collections.singletonList(Paths.get(target.toURI())));
        c.setTargetFolder(Paths.get("output"));
        c.setReportGenerator(new TextReportGenerator(new PrintWriter(System.out)));
        final MutationRunner r = new MutationRunner(c);
        r.run();
    }
}
