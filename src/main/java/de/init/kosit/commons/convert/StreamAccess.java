package de.init.kosit.commons.convert;

import javax.xml.stream.XMLStreamReader;

/**
 * Marker interface for {@link javax.xml.bind.Unmarshaller.Listener} implementation needing access to the actual stream
 * reader while processing an xml input.
 * 
 * @author apenski
 */
public interface StreamAccess {

    /**
     * Sets the stream reader used during unmarshalling.
     * @param reader the stream reader
     */
    void setStreamReader(XMLStreamReader reader);
}
