package de.tuberlin.dima.schubotz.fse;

import java.io.InputStream;
import java.util.Scanner;

import eu.stratosphere.core.fs.Path;

public class TestUtils {
    private final static String testQueryString = getFileContents("fQuery.xml");

    public static String getTestQueryString() {
        return testQueryString;
    }

    public static String getTestFile10() {
        return getFileContents("test10.xml");
    }

    public static String getTestFile1() {
        String[] split = getTestFile10().split(MainProgram.DOCUMENT_SEPARATOR,2);
        return split[0];
    }
    
    public static Path getTestFile10Path() {
    	return new Path("test10.xml");
    }

    static String getFileContents(String fname) {
        InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(fname);
        Scanner s = new Scanner(is, "UTF-8");
        s.useDelimiter("\\A");
        String out = s.hasNext() ? s.next() : "";
        s.close();
        return out;
    }
}
