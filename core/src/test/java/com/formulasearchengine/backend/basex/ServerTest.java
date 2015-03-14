package com.formulasearchengine.backend.basex;

import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ServerTest {
	private Server srv;
	private static boolean running = false;

	public ServerTest() throws IOException {
		srv = new Server();
	}

	@After
	public void shutDown() throws IOException, InterruptedException {
		srv.shutdown();
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

}