package de.kosit.xmlmutate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.kosit.xmlmutate.mutator.MutatorException;

/**
 * Unit test for simple App.
 */
public class XMLMutateConfigurationTest {

    private XMLMutateConfigurationImpl config = null;

    @BeforeEach
    void init() {
        this.config = new XMLMutateConfigurationImpl();
    }

    @Test
    @DisplayName("Check that default OutputDir is correct")
    void defaultOutputDirTest() {
        Path cwd = Paths.get(System.getProperty("user.home")).normalize().toAbsolutePath();
        assertEquals(cwd, config.getOutputDir());
    }

    @Test
    @DisplayName("Default XSD cache is empty")
    void defaultXSDCache() {
        assertFalse(this.config.hasSchema());
    }

    @Test
    void defaultRunMode() {
        assertEquals(RunModeEnum.GENERATE, config.getRunMode());
    }


    @Test
    @DisplayName("configure schema")
    void configureSchema() {
        config.addSchema("ubl", "c:/data/git-repos/validator-configuration-xrechnung/build/resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd");
        assertTrue(config.hasSchema(), "Schema schould be available!");
    }


    @Test
    @DisplayName("Reject null schema name")

    void nullSchemaName() {
        assertThrows(NullPointerException.class, () -> {
            config.addSchema(null,null);
        });
    }

    @Test
    @DisplayName("Reject empty schema name")

    void rejectEmptySchemaName() {
        assertThrows(IllegalArgumentException.class, () -> {
            config.addSchema("", "");
        });
    }

    @Test
    @DisplayName("Reject wrong schema file")

    void rejectWrongSchemaFile() {
        assertThrows(MutatorException.class, () -> {
            config.addSchema("ubl", "");
        });
    }
}
