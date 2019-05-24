package de.kosit.xmlmutate.mutation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.validation.Schema;

import lombok.Getter;
import lombok.Setter;

import de.kosit.xmlmutate.report.ReportGenerator;

/**
 * @author Andreas Penski
 */
@Getter
@Setter
public class RunnerConfig {

    private Path targetFolder;

    private List<Path> documents;

    private Schema schema;

    private List<Schematron> schematronRules = new ArrayList<>();

    private ReportGenerator reportGenerator;

    public NameGenerator nameGenerator;
}
