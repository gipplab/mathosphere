package de.tuberlin.dima.schubotz.common.utils;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.Scanner;

/**
 * Test class for XML comparison methods.
 * This should test all methods of comparison,
 * not just the one being used.
 */
public class ComparisonHelperTest {
    //TODO merge this with TestUtils' method
    private String getFileAsString(String filename) throws IOException {
        InputStream resource = ComparisonHelperTest.class.getClassLoader().getResourceAsStream(filename);
        if (resource == null) {
            //Try again with absolute path
            //Throws FileNotFound exception
            resource = new BufferedInputStream(new FileInputStream(filename));
        }
        try {
            //Stupid scanner tricks to read the entire file as one token
            final Scanner s = new Scanner(resource).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }finally {
            resource.close();
        }
    }
    @Test
    public void testQvarDifferent() {

    }

    @Test
    public void testQvarSimilar() {

    }
    @Test
    public void testQvarIdentical() {

    }
    @Test
    public void testSubtreeIdentical() {

    }
}
