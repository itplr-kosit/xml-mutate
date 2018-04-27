package de.kosit.xmlmutate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.transform.Templates;

import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test for command line input to XMLMutaTe.
 */
public class XMLMutateAppTest {

    private XMLMutateApp app = null;
    private static final Logger log = Logger.getLogger(XMLMutateAppTest.class.getName());

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
    // @Disabled
    @DisplayName("Default mutate only run on a single test xml instance")

    void defaultMutateRunOnSingleInstance() throws URISyntaxException {
        log.info("testing stuff");
        // full file path to test xml instance valthough it is on classpath
        Class<? extends Object> clazz = this.getClass();
        assertNotNull(clazz);
        URL url = clazz.getResource("/01.01a-INVOICE_ubl.xml");

        assertNotNull(url, "URL is null");
        log.info("url= " + url.getFile());
        //need new app instance
        //url.getFile().toString()/
        this.app = new XMLMutateApp(
                new String[] { "D:/git-repos/xml-mutator/target/test-classes/ubl-invoice-empty-mutation-tests.xml",
                        "D:/git-repos/xml-mutator/target/test-classes/ubl-invoice-remove-mutation-tests.xml",
                        "D:/git-repos/xml-mutator/src/test/resources/ubl-invoice-add-mutation-tests.xml" });
        assertEquals(0, this.app.run(), "Posix return code is 0 for success");
    }
}
