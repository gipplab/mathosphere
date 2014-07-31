package de.tuberlin.dima.schubotz.common.utils;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static junit.framework.TestCase.assertEquals;


/**
 * Test class for XML comparison methods.
 * This should test all methods of comparison,
 * not just the one being used.
 */
@RunWith(Parameterized.class)
public class ComparisonHelperTest {
    private String prefix;
    private double expectedScore;

    /**
     * Params in form of prefix, expected result.
     * Prefixes are the common prefix of the test resource
     * @return array of parameters
     */
    @Parameterized.Parameters
	public static Collection<Object[]> inputNumDocs() {
		return Arrays.asList(new Object[][]{
                {"de/tuberlin/dima/schubotz/common/utils/qvar.MML.Identical", 24.0}
        });
	}

    public ComparisonHelperTest(String prefix, double score) {
        this.prefix = prefix;
        this.expectedScore = score;
    }

    //TODO merge this with TestUtils' method and WikiAbstractSubprocess' method
    private static String getFileAsString(String filename) throws IOException {
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
    public void testComparison() throws IOException {
        final String in = getFileAsString(prefix + ".xml");
        final String compare = getFileAsString(prefix + ".compare.xml");
        //hack while coding better comparison
        final int numMatches = ComparisonHelper.calculateMMLScore(in, compare);
        assertEquals("Number of matches does not match expected", expectedScore, (double) numMatches);
    }
}
