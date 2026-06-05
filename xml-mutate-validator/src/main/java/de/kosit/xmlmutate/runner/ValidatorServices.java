package de.kosit.xmlmutate.runner;

import de.init.kosit.commons.ObjectFactory;
import de.init.kosit.commons.convert.ConversionService;
import de.init.kosit.commons.schematron.SchematronService;
import de.init.kosit.commons.transform.ExecutableRepository;
import de.init.kosit.commons.transform.TransformationService;
import de.init.kosit.commons.validate.SchemaRepository;
import de.init.kosit.commons.validate.SchemaValidationService;
import de.init.kosit.commons.validate.SchematronValidationService;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

/**
 * Validator-side service locator. Holds the schema + Schematron infrastructure
 * (SVRL JAXB registration, schema repository, validation services). Kept separate
 * from {@link Services} so the mutator side can be used without dragging in
 * schxslt / saxon validation at class-init time.
 */
public final class ValidatorServices {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidatorServices.class);
    private static final ExecutableRepository xsltRepository;
    private static final ConversionService conversionService;
    private static final SchemaValidationService schemaValidatonService;
    private static final TransformationService transformService;
    public static final SchematronService schematronService;
    private static final SchemaRepository schemaRepository;
    private static final SchematronValidationService schematronValidationService;

    private ValidatorServices() {
    }

    static {
        log.debug("Initializing validator services");
        xsltRepository = new ExecutableRepository(ObjectFactory.createProcessor());
        conversionService = new ConversionService();
        conversionService.initialize(SchematronOutput.class.getPackage().getName());
        schemaValidatonService = new SchemaValidationService(conversionService);
        transformService = new TransformationService(conversionService, schemaValidatonService, xsltRepository, ObjectFactory.createProcessor());
        schematronService = new SchematronService(xsltRepository, transformService);
        schemaRepository = new SchemaRepository();
        schematronValidationService = new SchematronValidationService();
    }

    public static SchemaValidationService getSchemaValidatonService() {
        return schemaValidatonService;
    }

    public static SchematronService getSchematronService() {
        return schematronService;
    }

    public static SchemaRepository getSchemaRepository() {
        return schemaRepository;
    }

    public static SchematronValidationService getSchematronValidationService() {
        return schematronValidationService;
    }
}
