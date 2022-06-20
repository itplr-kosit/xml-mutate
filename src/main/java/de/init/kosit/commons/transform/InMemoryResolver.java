package de.init.kosit.commons.transform;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import de.init.kosit.commons.artefact.Artefact;

/**
 * {@link URIResolver} der auf den in den Speicher eingelesen {@link Artefact Artefacten} arbeitet.
 * 
 * @author Andreas Penski (]init[ AG)
 */
class InMemoryResolver implements URIResolver {

    Map<String, byte[]> resolve;

    /**
     * Constructor mit den möglichen Zielen eines Resolving-Vorgangs.
     * 
     * @param artefacts die Artefacte
     */
    InMemoryResolver(final Collection<Artefact> artefacts) {
        this.resolve = new HashMap<>();
        artefacts.forEach(a -> this.resolve.put(a.getName(), a.getContent()));
    }

    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        final byte[] bytes = this.resolve.get(href);
        if (bytes != null) {
            return new StreamSource(new ByteArrayInputStream(bytes));
        }
        // Klärung mit Fabian
        if (href.equals("http://www.eclipse.org/uml2/5.0.0/UML/Profile/Standard")) {
            return new StreamSource(InMemoryResolver.class.getResourceAsStream("/profiles/Standard.profile.uml"));
        }

        return null;
    }
}
