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
     * @param indentSize
     * @return
     * @throws TransformerConfigurationException
     */
    public static Transformer createTransformer(int indentSize) throws TransformerConfigurationException {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        if (indentSize > 0) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indentSize));
        }

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        return transformer;
    }

}