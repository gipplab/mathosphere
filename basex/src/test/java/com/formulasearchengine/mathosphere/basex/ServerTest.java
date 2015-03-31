package com.formulasearchengine.mathosphere.basex;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class ServerTest  {
@Test
	public void restartServerTest() throws Exception {
		final URL fname = BaseXTestSuite.class.getClassLoader().getResource( "sampleHarvest.xml" );
		File file = new File(fname.toURI());
		Server srv = Server.getInstance( file );
		srv.shutdown();
		Server.getInstance( file );

	}

	@Test
	public void testImportData() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		Server.getInstance().runQuery( "count(./*/*)", ps );
		assertEquals("104",baos.toString("UTF-8"));
	}

}