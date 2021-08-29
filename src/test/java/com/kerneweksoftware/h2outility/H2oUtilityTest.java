package com.kerneweksoftware.h2outility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class H2oUtilityTest 
{

    @Test
    void test_getArchiveName_WithPath() {
        String examplePath = "example\\Archive.H2O";

        String name = H2oUtility.getArchiveName(examplePath);

        assertEquals("Archive.H2O", name);
    }

    @Test
    void test_getArchiveName_WithoutPath() {
        String examplePath = "Archive.H2O";

        String name = H2oUtility.getArchiveName(examplePath);

        assertEquals("Archive.H2O", name);
    }
}
