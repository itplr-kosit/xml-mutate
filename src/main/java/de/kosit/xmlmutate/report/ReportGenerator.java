package de.kosit.xmlmutate.report;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import de.kosit.xmlmutate.mutation.Mutation;

/**
 * @author Andreas Penski
 */
public interface ReportGenerator {

    void generate(List<Pair<Path, List<Mutation>>> results);

}
