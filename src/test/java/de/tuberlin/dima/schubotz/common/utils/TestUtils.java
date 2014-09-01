package de.tuberlin.dima.schubotz.common.utils;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import eu.stratosphere.core.fs.Path;

import java.io.*;
import java.util.Scanner;

public class TestUtils {
    public static String getTestQueryString() throws IOException {
        return getFileContents("de/tuberlin/dima/schubotz/fse/fQuery.xml");
    }

    public static String getTestFile10() throws IOException {
        return getFileContents("de/tuberlin/dima/schubotz/fse/test10.xml");
    }
	public static String getTestResultForTest11() throws IOException {
		return getFileContents( "expectedMatch.xml" );
	}
    public static String getTestFile1() throws IOException {
        String[] split = getTestFile10().split(MainProgram.DOCUMENT_SEPARATOR,2);
        return split[0];
    }
    
    public static Path getTestFile10Path() {
    	return new Path("de/tuberlin/dima/schubotz/fse/test10.xml");
    }

    static String getFileContents(String fname) throws IOException {
        final InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(fname);
        try {
            final Scanner s = new Scanner(is, "UTF-8");
            //Stupid scanner tricks to read the entire file as one token
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }finally{
            is.close();
        }
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
