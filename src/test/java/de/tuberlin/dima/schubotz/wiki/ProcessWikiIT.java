package de.tuberlin.dima.schubotz.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.tuberlin.dima.schubotz.wiki.preprocess.ProcessWiki;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.client.LocalExecutor;

@RunWith(Parameterized.class)
public class ProcessWikiIT {
	private String debugWikiInput;
	private String debugOutput;
	
	
	@Parameterized.Parameters
	public static Collection<Object[]> inputParam () {
		return Arrays.asList(new Object[][] 
				{{"file:///home/jjl4/augmentedWikiDump.xml", "file:///home/jjl4/"}}); 
				//{{"", "file:///home/jjl4/"}});
	}
	@SuppressWarnings("hiding")
	public ProcessWikiIT (String debugWikiInput, String debugOutput) {
		this.debugWikiInput = debugWikiInput;
		this.debugOutput = debugOutput;
	}
	@Test
	public void TestProcessWiki() throws Exception{
		String debugLatexOutput = debugOutput + "latexWikiMap.csv";
		String debugNumWikiOutput = debugOutput + "numWiki.txt";
		if (debugWikiInput.equals("")) {
			debugWikiInput = "file://" + getClass().getClassLoader().getResources("de.tuberlin.dima.schubotz.wiki/mappers/sampleWikiDump.xml").nextElement().getPath();
		}
		try {
			String wikiQueryInput = "file://" + getClass().getClassLoader().getResources("de/tuberlin/dima/schubotz/wiki/mappers/wikiQuery.xml").nextElement().getPath();
			ProcessWiki.parseArgs(new String[]{"16",
											   debugWikiInput,
											   wikiQueryInput,
											   debugLatexOutput,
											   debugNumWikiOutput,
											   "debug"});
		} catch (IOException e) {
			e.printStackTrace();
			fail("Missing input files IO Exception");
			return;
		}
		try {
			ProcessWiki.ConfigurePlan();
	        ExecutionEnvironment env = ProcessWiki.getExecutionEnvironment();
	        Plan plan = env.createProgramPlan();
	        LocalExecutor.execute(plan);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Execution error!");
			return;
		}
		//Check to make sure correct file output
		BufferedReader br = null; 
		try {
			br = new BufferedReader(new FileReader(new File(new URI(debugLatexOutput).getPath())));
			assertEquals(Boolean.valueOf(br.readLine() != null), true);
			br = new BufferedReader(new FileReader(new File(new URI(debugNumWikiOutput).getPath())));
			assertEquals(Boolean.valueOf(br.readLine() != null), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Files not outputted or given directory is incorrect.");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException for output file");
			return;
		} finally {
			br.close();
		}
	}
	

}
