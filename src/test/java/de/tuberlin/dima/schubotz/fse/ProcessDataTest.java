package de.tuberlin.dima.schubotz.fse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.tuberlin.dima.schubotz.fse.preprocess.ProcessData;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.client.LocalExecutor;

@RunWith(Parameterized.class)
public class ProcessDataTest {
	private Integer numDocs;
	private String inputFile;
	private String debugOutput;
	
	@Parameterized.Parameters
	public static Collection<Object[]> inputParam() {
		return Arrays.asList(new Object[][] {
			{9999,"test10000.xml","/home/jjl4/"} //DEBUG test parameters
		});
	}
	public ProcessDataTest(Integer numDocs, String inputFile, String debugOutput) {
		this.numDocs = numDocs;
		this.inputFile = inputFile;
		this.debugOutput = debugOutput;
	}
	@Test
	public void TestProcessData() throws Exception {
		String keywordDocsFilename;
		String latexDocsFilename;
		String numDocsFilename;
		try {
			String inputFilename = "file://" + getClass().getClassLoader().getResources(inputFile).nextElement().getPath();
			System.out.println("ProcessData testing on: " + inputFilename);
			String queryFile = "file://" + getClass().getClassLoader().getResources("fQuery.xml").nextElement().getPath();
			if (!debugOutput.equals("")) {
				keywordDocsFilename = debugOutput + "keywordDocsMap.csv";
				latexDocsFilename = debugOutput + "latexDocsMap.csv";
				numDocsFilename = debugOutput + "numDocs.txt";
			} else {
				keywordDocsFilename = "file://" + getClass().getClassLoader().getResources("keywordDocsMap.csv").nextElement().getPath();
				latexDocsFilename = "file://" + getClass().getClassLoader().getResources("latexDocsMap.csv").nextElement().getPath();
		        numDocsFilename = "file://" + getClass().getClassLoader().getResources("numDocs.txt").nextElement().getPath();
			}
			ProcessData.parseArg(new String[]{"16",
											  inputFilename,
											  queryFile,
											  keywordDocsFilename,
											  latexDocsFilename,
											  numDocsFilename});
			ProcessData.ConfigurePlan();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		try {
	        ExecutionEnvironment env = ProcessData.getExecutionEnvironment();
	        Plan plan = env.createProgramPlan();
	        LocalExecutor.execute(plan);
		} catch (Exception e) {
			fail("Execution error.");
			e.printStackTrace();
			return;
		}
		
		//Check to make sure correct file output
		BufferedReader br = null; 
		try {
			br = new BufferedReader(new FileReader(new File(numDocsFilename)));
			assertEquals(Integer.valueOf(br.readLine()),numDocs);
			br = new BufferedReader(new FileReader(new File(keywordDocsFilename)));
			assertNotNull(br.readLine());
			br = new BufferedReader(new FileReader(new File(latexDocsFilename)));
			assertNotNull(br.readLine());
		} catch (FileNotFoundException e) {
			fail("Files not outputted or given directory is incorrect.");
			e.printStackTrace();
		} finally {
			br.close();
		}
		
		
	}

}
