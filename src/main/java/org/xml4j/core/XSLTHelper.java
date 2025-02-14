package org.xml4j.core;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * XSLTHelper
 */
public class XSLTHelper {

    /**
     * Creates an Identity Transformer.
     * 
     * @param i
     * @return
     * @throws TransformerConfigurationException
     */
    public static Transformer createTransformer(int i) throws TransformerConfigurationException {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        if (i > 0) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(i));
        }

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        return transformer;
    }

}