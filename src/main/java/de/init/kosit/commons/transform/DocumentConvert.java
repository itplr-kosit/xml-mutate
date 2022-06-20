package de.init.kosit.commons.transform;

import org.w3c.dom.Document;

import de.init.kosit.commons.ObjectFactory;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.Destination;

/**
 * @author Andreas Penski
 */
class DocumentConvert implements Convert<Document> {

    private Document owner;

    public DocumentConvert() {
        owner = ObjectFactory.createDocumentBuilder(false).newDocument();

    }

    @Override
    public Destination createDestination() {
        return new DOMDestination(owner);
    }

    @Override
    public Document getResult() {
        return owner;
    }
}
