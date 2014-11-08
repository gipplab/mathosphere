package com.formulasearchengine.backend.basex;

import org.junit.Test;

public class BenchmarkTest {

	@Test
	public void testMain () throws Exception {
		String[] args = new String[7];
		final ClassLoader classLoader = getClass().getClassLoader();
		args[0] = "-q";
		args[1] = classLoader.getResource( "Ntcir11MathWikipediaTopicsParticipants.xml" ).getFile();
		args[2] = "-d";
		args[3] = classLoader.getResource( "sampleHarvest.xml" ).getFile();
		args[4] = "-o";
		args[5] = classLoader.getResource( "exampleOutput.txt" ).getFile();
		args[6] = "-c";
		Benchmark.main( args );
	}
}