// Generated by delombok at Fri Aug 13 16:11:22 CEST 2021
package de.init.kosit.commons.schematron;

import java.net.URI;

import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.w3c.dom.Document;

import de.init.kosit.commons.transform.ExecutableRepository;
import de.init.kosit.commons.transform.Transform;
import de.init.kosit.commons.transform.TransformationService;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Schematron
 *
 * @author Andreas Penski
 */
//@ApplicationScoped
public class SchematronService {
    //@Inject
    private ExecutableRepository executableRepository;
    //@Inject
    private TransformationService transformService;

    /**
     * @param schematron
     * @param document
     * @return
     */
    public SchematronOutput validate(final URI schematron, final Document document) {
        final Transform<SchematronOutput> t = Transform.create(getExecutable(schematron)).convertTo(SchematronOutput.class).build();
        return transformService.transform(t, document).getObject();
    }

    private XsltExecutable getExecutable(final URI uri) {
        return executableRepository.createExecutable(uri, null);
    }

    @java.lang.SuppressWarnings("all")
    public SchematronService() {
    }

    @java.lang.SuppressWarnings("all")
    public SchematronService(final ExecutableRepository executableRepository, final TransformationService transformService) {
        this.executableRepository = executableRepository;
        this.transformService = transformService;
    }
}
