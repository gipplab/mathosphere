package com.formulasearchengine.mathosphere.basex;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ServerTest {
	private Server srv;

	public ServerTest() throws IOException {
		srv = new Server();
	}

	@Test
	public void testImportData() throws Exception {
		//noinspection ConstantConditions
		final String file = getClass().getClassLoader().getResource( "sampleHarvest.xml" ).getFile();
		srv.importData( file );
		System.out.println( "Imported" + file );
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		srv.runQuery( "count(./*/*)", ps );
		assertEquals("104",baos.toString("UTF-8"));
	}
	@Test(expected = IOException.class)
	public void testInvaildData()  throws Exception {
		srv.importData( ">invalid<" );
	}

}