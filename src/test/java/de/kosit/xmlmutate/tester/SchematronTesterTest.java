package de.kosit.xmlmutate.tester;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import de.kosit.xmlmutate.XMLMutateManufactory;
import de.kosit.xmlmutate.mutator.MutatorException;

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
    @DisplayName("Succesful init of SchematronTester")
    void testSchematronTesterInit()  {
        this.schematron = XMLMutateManufactory.fileFromClasspath("XRechnung-UBL-validation-Invoice.xsl");
        new SchematronTester("xr-ubl-in", this.schematron);
    }

    @Test
    @DisplayName("Fail init of SchematronTester on empty file name")

    void testSchematronTesterFailWrongXSLTFile() throws IOException {
        assertThrows(MutatorException.class, () -> {
            new SchematronTester("xr-ubl-in", "");
        });
    }

    @Test
    @DisplayName("Fail init of SchematronTester on wrong file name")

    void testSchematronTesterFailXSLTFile() throws IOException {
        assertThrows(MutatorException.class, () -> {
            new SchematronTester("xr-ubl-in", "hhh");
        });
    }

    @Test
    @DisplayName("Fail init of SchematronTester on NULL file name")

    void testSchematronTesterFailNullXSLTFile() throws IOException {
        assertThrows(MutatorException.class, () -> {
            new SchematronTester("xr-ubl-in", null);
        });
    }

    @Test
    @DisplayName("Succesful schematron test with failed asserts")
    // @Disabled
    void schematronTesterTestSuccess() {
        this.schematron = XMLMutateManufactory.fileFromClasspath("XRechnung-UBL-validation-Invoice.xsl");
        SchematronTester st = new SchematronTester("xr-ubl-in", this.schematron);
        String file = XMLMutateManufactory.fileFromClasspath("ubl-invoice-empty-mutation-tests.xml");
        Document doc = XMLMutateManufactory.domDocumentFromFileName(file);
        List<TestItem> report = st.test(doc, null);
        assertFalse(report.isEmpty());
    }
}