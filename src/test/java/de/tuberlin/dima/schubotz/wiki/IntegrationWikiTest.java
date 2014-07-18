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

import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.client.LocalExecutor;


@RunWith(Parameterized.class)
public class IntegrationWikiTest {
	private Integer numWiki;
	private String inputDir;
	private String outputDir;
	private Boolean debug;

	@Parameterized.Parameters
	public static Collection<Object[]> inputNumDocs() {
		return Arrays.asList(new Object[][] {
				{new Integer(9999),"/home/jjl4/","/home/jjl4/", Boolean.valueOf(true)} 
		});
	}
	@SuppressWarnings("hiding")
	public IntegrationWikiTest(Integer numWiki, String inputDir, String outputDir, Boolean debug) {
		this.numWiki = numWiki;
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.debug = debug;
	}

	@Test
	public void TestLocalExecution() throws Exception {
		String wikiInput = inputDir + "augmentedWikiDump.xml";
		String wikiMapInput = inputDir + "latexWikiMap.csv";
		String wikiOutput = outputDir + "wikiProgramOutput.csv";
		try {
			String wikiQueryInput = "" + getClass().getClassLoader().getResources("wikiQuery.xml").nextElement().getPath();
			WikiProgram.parseArgs(new String[] {"16",
											  wikiOutput,
											  wikiInput,
											  wikiQueryInput,
											  wikiMapInput,
											  String.valueOf(numWiki),
											  debug.toString()
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Missing input files IO Exception");
			return;
		}
		
		try {
			WikiProgram.ConfigurePlan();
	        ExecutionEnvironment env = WikiProgram.getExecutionEnvironment();
	        Plan plan = env.createProgramPlan();
	        LocalExecutor.execute(plan);
		} catch (Exception e) {
			e.printStackTrace();
			fail("execution failed");
			return;
		}
		
		//Check to make sure correct file output
		BufferedReader br = null; 
		try {
			br = new BufferedReader(new FileReader(new File(new URI(wikiOutput).getPath())));
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