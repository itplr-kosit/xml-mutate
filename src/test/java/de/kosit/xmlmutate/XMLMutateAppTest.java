package de.kosit.xmlmutate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.transform.Templates;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test for command line input to XMLMutaTe.
 */
public class XMLMutateAppTest {

    private XMLMutateApp app = null;
    private static final Logger log = LogManager.getLogger(XMLMutateAppTest.class);
    private static String testXMLInstance = "";

    @BeforeAll
    static void configure() throws IOException {

        XMLMutateAppTest.testXMLInstance = XMLMutateManufactory.fileFromClasspath("ubl-invoice-add-mutation-tests.xml");
        log.info("url= " + testXMLInstance);
    }



    @BeforeEach
    void init() {
        this.app = new XMLMutateApp();
    }

    @Test
    void loadallXSLT() {
        log.info("Test loading XSLT");
        // full file path to test xml instance valthough it is on classpath
        Map<String, Templates> map = app.loadAllTransformer();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("add"), "Assumed key add to be present");
    }

    @Test
    @DisplayName("Test MutATest=mutate and test run mode")
    void mutATestOnSingleInstance() {
        // need new app instance for testing with CLI input
        log.debug("Testing validation");
        this.app = new XMLMutateApp(new String[] { "--run-mode", "test", "--schema", "ubl",
                "D:/git-repos/validator-configuration-xrechnung/build/resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd",
                testXMLInstance });
        assertEquals(app.getConfiguration().getRunMode(), RunModeEnum.TEST);
    }

    @Test
    // @Disabled
    @DisplayName("Default mutate only run on a single test xml instance")

    void defaultMutateRunOnSingleInstance() throws URISyntaxException {

        // need new app instance for testing with CLI inpu
        this.app = new XMLMutateApp(new String[] { testXMLInstance });

        assertEquals(0, this.app.run(), "Posix return code is 0 for success");
    }
}
