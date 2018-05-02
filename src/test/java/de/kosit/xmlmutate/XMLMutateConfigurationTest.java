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
    @DisplayName("Reject that output dir with null values gets rejected")

    void nullOutputDirTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            config.setOutputDir(null);
        });
    }

}
