package de.tuberlin.dima.schubotz.wiki;

import static org.junit.Assert.*;

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

import de.tuberlin.dima.schubotz.fse.preprocess.ProcessData;
import de.tuberlin.dima.schubotz.wiki.preprocess.ProcessWiki;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.client.LocalExecutor;

@RunWith(Parameterized.class)
public class ProcessWikiTest {
	private String debugWikiInput;
	private String debugOutput;
	
	
	@Parameterized.Parameters
	public static Collection<Object[]> inputParam () {
		return Arrays.asList(new Object[][] 
				{{"file:///home/jjl4/sampleWikiDump.xml", "file:///home/jjl4/"}}); //DEBUG test parameters
	}
	public ProcessWikiTest (String debugWikiInput, String debugOutput) {
		this.debugWikiInput = debugWikiInput;
		this.debugOutput = debugOutput;
	}
	@Test
	public void TestProcessWiki() throws Exception{
		String debugLatexOutput = debugOutput + "latexWikiMap.csv";
		String debugNumWikiOutput = debugOutput + "numWiki.txt";
		try {
			String wikiQueryInput = "file://" + getClass().getClassLoader().getResources("wikiQuery.xml").nextElement().getPath();
			ProcessWiki.parseArgs(new String[]{"16",
											   debugWikiInput,
											   wikiQueryInput,
											   debugLatexOutput,
											   debugNumWikiOutput,
											   "debug"});
		} catch (IOException e) {
			fail("Missing input files IO Exception");
			e.printStackTrace();
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
			String line;
			boolean containsAlbedo = false;
			boolean containsMean = false;
			while ((line = br.readLine()) != null) {
				if (line.contains("Albedo")) {
					containsAlbedo = true;
				} else if (line.contains("Arithmetic mean")) {
					containsMean = true;
				}
			}
			assertEquals(containsAlbedo && containsMean, true);
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
