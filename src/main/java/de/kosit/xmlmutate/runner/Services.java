package de.kosit.xmlmutate.runner;

import org.oclc.purl.dsdl.svrl.SchematronOutput;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.convert.ConversionService;
import de.init.kosit.commons.schematron.SchematronService;
import de.init.kosit.commons.transform.ExecutableRepository;
import de.init.kosit.commons.transform.TransformationService;
import de.init.kosit.commons.validate.SchemaValidationService;
import de.kosit.xmlmutate.mutator.MutatorRegistry;

/**
 * Common Service Definition.
 * 
 * @author Andreas Penski
 */

public class Services {

    public static ExecutableRepository xsltRepository;

    public static ConversionService conversionService;

    public static SchemaValidationService schemaValidatonService;

    public static TransformationService transformService;

    public static SchematronService schematronService;

    private static final MutatorRegistry registry = MutatorRegistry.getInstance();

    static {
        xsltRepository = new ExecutableRepository(ObjectFactory.createProcessor());
        conversionService = new ConversionService();
        conversionService.initialize(SchematronOutput.class.getPackage().getName());
        schemaValidatonService = new SchemaValidationService(conversionService);
        transformService = new TransformationService(conversionService, schemaValidatonService, xsltRepository,
                ObjectFactory.createProcessor());
        schematronService = new SchematronService(xsltRepository, transformService);

    }

    public static MutatorRegistry getRegistry() {
        return registry;
    }
}
