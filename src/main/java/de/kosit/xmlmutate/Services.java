package de.kosit.xmlmutate;

import org.oclc.purl.dsdl.svrl.SchematronOutput;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.convert.ConversionService;
import de.init.kosit.commons.schematron.SchematronService;
import de.init.kosit.commons.transform.ExecutableRepository;
import de.init.kosit.commons.transform.TransformationService;
import de.init.kosit.commons.validate.SchemaValidationService;

/**
 * Common Service Definition.
 * 
 * @author Andreas Penski
 */

public class Services {

    public static final ExecutableRepository xsltRepository;

    public static final ConversionService conversionService;

    public static final SchemaValidationService schemaValidatonService;

    public static final TransformationService transformService;

    public static final SchematronService schematronService;

    static {
        xsltRepository = new ExecutableRepository(ObjectFactory.createProcessor());
        conversionService = new ConversionService();
        conversionService.initialize(SchematronOutput.class.getPackage().getName());
        schemaValidatonService = new SchemaValidationService(conversionService);
        transformService = new TransformationService(conversionService, schemaValidatonService, xsltRepository,
                ObjectFactory.createProcessor());
        schematronService = new SchematronService(xsltRepository, transformService);

    }
}
