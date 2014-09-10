package de.tuberlin.dima.schubotz.utils;

import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import org.apache.flink.core.fs.Path;
import org.junit.Assume;

import java.io.*;
import java.util.Scanner;

public class TestUtils {
    public static String getTestQueryString() throws IOException {
        return getFileContents("de/tuberlin/dima/schubotz/fse/fQuery.xml");
    }

    public static String getTestFile10String() throws IOException {
        return getFileContents("de/tuberlin/dima/schubotz/fse/test10.xml");
    }
    public static String getTestFile10StringPath() {
        return "de/tuberlin/dima/schubotz/fse/test10.xml";
    }
    public static String getFQueryStringPath() {
        return "de/tuberlin/dima/schubotz/fse/fQuery.xml";
    }

	public static String getTestResultForTest11() throws IOException {
		return getFileContents("expectedMatch.xml");
	}
    /*
    public static String getTestFile1() throws IOException {
        String[] split = getTestFile10().split(MainProgram.DOCUMENT_SEPARATOR,2);
        return split[0];
    }
    */
    
    public static Path getTestFile10Path() {
    	return new Path("de/tuberlin/dima/schubotz/fse/test10.xml");
    }

    public static String getFileContents(String fName) throws IOException {
        final InputStream is = getClassLoader().getResourceAsStream(fName);
        try {
            final Scanner s = new Scanner(is, "UTF-8");
            //Stupid scanner tricks to read the entire file as one token
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }finally{
            is.close();
        }
    }

	public static String setTestPassword (){
		if (getClassLoader().getResource( "testpassword") == null){
			Assume.assumeTrue( false );
		}
		String testPassword = null;
		try {
			testPassword = getFileContents( "testpassword" );
		} catch ( IOException e ) {
			e.printStackTrace();
			Assume.assumeTrue( false );
		}
		Assume.assumeFalse( testPassword == null );
		Settings.setProperty( SettingNames.PASSWORD, testPassword );
		return testPassword;
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

	public static void setTestQueries () {
		String fName = getClassLoader().
			getResource( "de/tuberlin/dima/schubotz/fse/fQuery.xml" ).toString();
		Settings.setProperty( SettingNames.QUERY_FILE, fName );
	}

	public static ClassLoader getClassLoader () {
		return TestUtils.class.getClassLoader();
	}
}
