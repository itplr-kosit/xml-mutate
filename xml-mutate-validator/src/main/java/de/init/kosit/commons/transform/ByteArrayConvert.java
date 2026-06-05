package de.init.kosit.commons.transform;

import java.io.ByteArrayOutputStream;

import de.init.kosit.commons.ObjectFactory;

import net.sf.saxon.s9api.Destination;

/**
 * @author Andreas Penski
 */
class ByteArrayConvert implements Convert<byte[]> {

    private ByteArrayOutputStream outputStream;

    @Override
    public Destination createDestination() {
        outputStream = new ByteArrayOutputStream();
        return ObjectFactory.createProcessor().newSerializer(outputStream);
    }

    @Override
    public byte[] getResult() {
        return outputStream.toByteArray();
    }
}
