package de.kosit.xmlmutate.report;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.runner.FailureMode;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Andreas Penski
 */
public interface ReportGenerator {

    void generate(List<Pair<Path, List<Mutation>>> results, FailureMode failureMode);

}
