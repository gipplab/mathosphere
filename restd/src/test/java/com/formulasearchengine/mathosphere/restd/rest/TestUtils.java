package com.formulasearchengine.mathosphere.restd.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by jjl4 on 6/29/15.
 */
public class TestUtils {
	static String BASEX_RESOURCE_DIR = "com/formulasearchengine/mathosphere/basex/";

	public static String getFileContents( String fname ) throws IOException {
		try ( InputStream is = TestUtils.class.getClassLoader().getResourceAsStream( fname ) ) {
			final Scanner s = new Scanner( is, "UTF-8" );
			//Stupid scanner tricks to read the entire file as one token
			s.useDelimiter( "\\A" );
			return s.hasNext() ? s.next() : "";
		}
	}
}
