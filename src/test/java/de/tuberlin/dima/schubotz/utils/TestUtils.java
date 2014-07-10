package de.tuberlin.dima.schubotz.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import eu.stratosphere.core.fs.Path;

public class TestUtils {
    private final static String testQueryString = getFileContents("fQuery.xml");

    public static String getTestQueryString() {
        return testQueryString;
    }

    public static String getTestFile10() {
        return getFileContents("test10.xml");
    }
	public static String getTestResultForTest11(){
		return getFileContents( "expectedMatch.xml" );
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
    
    public static int countLines(String filename) throws IOException {
    	InputStream is = new BufferedInputStream(new FileInputStream(filename));
    	try {
    		byte[] c = new byte[1024];
    		int count = 0;
    		int readChars = 0;
    		boolean readingLine = false;
    		while((readChars = is.read(c)) != -1) {
    			readingLine = true;
    			for (int i=0; i < readChars; i++) {
    				if(c[i] == '\n') {
    					readingLine = false;
    					count++;
    				}
    			}
    		}
    		return readingLine ? ++count : count;
    	} catch (FileNotFoundException e) {
    		System.out.println("File not found or given directory is incorrect.");
    		e.printStackTrace();
    		return 0;
    	} finally {
    		is.close();
    	}
    }
}
