package de.kosit.xmlmutate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Check that default values are correct")
    @Disabled
    void defaultAppConfigTest() {

    }

    @Test
    void defaultOutputDirTest() {
        Path cwd = Paths.get(System.getProperty("user.home")).normalize().toAbsolutePath();
        assertEquals(cwd, config.getOutputDir());
    }

    @Test
    void defaultRunMode() {
        assertEquals("mutate", config.getRunMode());
    }

    @Test
    @DisplayName("Reject that output dir with null values gets rejected")

    void nullOutputDirTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            config.setOutputDir(null);
        });
    }

}
