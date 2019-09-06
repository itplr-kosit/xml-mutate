package de.kosit.xmlmutate.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ErrorCodeTest
 */
public class ErrorCodeTest {

    @Test
    @Tag("current")
    void testGeneralError() {
        // GENERAL_ERROR("Error: {0}");
        String test = String.format("Error: {0}", "hi");
        assertEquals(ErrorCode.GENERAL_ERROR.message("hi"), test);
    }

}
