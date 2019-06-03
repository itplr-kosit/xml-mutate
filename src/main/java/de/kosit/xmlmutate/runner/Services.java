package de.kosit.xmlmutate.runner;

import org.oclc.purl.dsdl.svrl.SchematronOutput;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.convert.ConversionService;
import de.init.kosit.commons.schematron.SchematronService;
import de.init.kosit.commons.transform.ExecutableRepository;
import de.init.kosit.commons.transform.TransformationService;
import de.init.kosit.commons.validate.SchemaValidationService;
import de.kosit.xmlmutate.mutation.NameGenerator;
import de.kosit.xmlmutate.mutation.SequenceNameGenerator;
import de.kosit.xmlmutate.mutator.MutatorRegistry;

/**
 * Common Service Definition.
 * 
 * @author Andreas Penski
 */

public class Services {

    private static final ExecutableRepository xsltRepository;

    private static final ConversionService conversionService;

    private static final SchemaValidationService schemaValidatonService;

    private static final TransformationService transformService;

    public static final SchematronService schematronService;

    private static final MutatorRegistry registry = MutatorRegistry.getInstance();

    private static final NameGenerator nameGenerator = new SequenceNameGenerator();

    static {
        xsltRepository = new ExecutableRepository(ObjectFactory.createProcessor());
        conversionService = new ConversionService();
        conversionService.initialize(SchematronOutput.class.getPackage().getName());
        schemaValidatonService = new SchemaValidationService(conversionService);
        transformService = new TransformationService(conversionService, schemaValidatonService, xsltRepository,
                ObjectFactory.createProcessor());
        schematronService = new SchematronService(xsltRepository, transformService);
    }

    public static SchemaValidationService getSchemaValidatonService() {
        return schemaValidatonService;
    }

    public static SchematronService getSchematronService() {
        return schematronService;
    }

    public static MutatorRegistry getRegistry() {
        return registry;
    }

    public static NameGenerator getNameGenerator() {
        return nameGenerator;
    }
}
