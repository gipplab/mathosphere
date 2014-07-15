package de.tuberlin.dima.schubotz.fse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.tuberlin.dima.schubotz.utils.TestUtils;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.client.LocalExecutor;

@RunWith(Parameterized.class)
public class IntegrationTest {
	private Integer numDocs;
	private String inputFile;
	private String outputDir;
	private Boolean debug;
	
	@Parameterized.Parameters
	public static Collection<Object[]> inputNumDocs() {
		return Arrays.asList(new Object[][] {
			{new Integer(9999),"test10000.xml","/home/jjl4/", Boolean.valueOf(true)} //DEBUG test parameters
		});
	}
	@SuppressWarnings("hiding")
	public IntegrationTest(Integer numDocs, String inputFile, String output, Boolean debug) {
		this.numDocs = numDocs;
		this.inputFile = inputFile;
		this.outputDir = output;
		this.debug = debug;
	}
    @Test
    public void TestLocalExecution() throws Exception {
    	String keywordDocsFilename="";
    	String latexDocsFilename="";
    	String outputFilename;
    	try {
	        String inputFilename = "file://" + getClass().getClassLoader().getResources(inputFile).nextElement().getPath(); 
	        System.out.println("Integration testing on: " + inputFilename);
	        String queryFile = "file://" + getClass().getClassLoader().getResources("fQuery.xml").nextElement().getPath();
	        if (!outputDir.equals("")) {
	        	keywordDocsFilename = outputDir + "keywordDocsMap.csv";
	        	latexDocsFilename = outputDir + "latexDocsMap.csv";
	        	outputFilename = outputDir + "output.csv";
	        	
	        } else {
		        keywordDocsFilename = "file://" + getClass().getClassLoader().getResources("keywordDocsMap.csv").nextElement().getPath();
		        latexDocsFilename = "file://" + getClass().getClassLoader().getResources("latexDocsMap.csv").nextElement().getPath();
		        outputFilename = "file://" + getClass().getClassLoader().getResources("test.out").nextElement().getPath();
	        }
	        outputFilename +=  + Math.random() * Integer.MAX_VALUE;
	        MainProgram.parseArg(new String[]{"16", inputFilename,
	        								  queryFile, outputFilename,
	        								  keywordDocsFilename, latexDocsFilename,
	        								  numDocs.toString(),
	        								  (debug.booleanValue() ? "debug" : "")}); 
	        MainProgram.ConfigurePlan();
        } catch (Exception e) {
        	fail("File IO/Configuration error. Check parameters, plan configuration.");
        	e.printStackTrace();
        	return;
        }
        try {
	        ExecutionEnvironment env = MainProgram.getExecutionEnvironment();
	        Plan plan = env.createProgramPlan();
	        LocalExecutor.execute(plan);
        } catch (Exception e) {
        	fail("Execution error. Check execution, add fault tolerance.");
        	e.printStackTrace();
        	return;
        }
        
        //read output file, check if correct number of lines
        try {
        	if (numDocs >= 1000) {
        		assertEquals(TestUtils.countLines(outputFilename),1000 * 50);
        	} else {
        		assertEquals(TestUtils.countLines(outputFilename),numDocs * 50);
        	}
        	
        } catch (IOException e) {
        	fail("Output file error");
    		e.printStackTrace();
    		return;
        }
        
    }
}
