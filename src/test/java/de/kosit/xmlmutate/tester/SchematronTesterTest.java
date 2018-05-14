package de.kosit.xmlmutate.tester;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import de.kosit.xmlmutate.XMLMutateManufactory;

/**
 * SchematronTesterTest
 *
 * @author Renzo Kottmann
 */
public class SchematronTesterTest {

    private final static Logger log = LogManager.getLogger(SchematronTesterTest.class);
    String schematron = "";
    Document doc = null;

    @Test
    void testSchematronValidation() throws IOException {
        this.schematron = XMLMutateManufactory.fileFromClasspath("XRechnung-UBL-validation-Invoice.xsl");
    }

}