package de.tuberlin.dima.schubotz.fse;

import org.junit.Test;

import de.tuberlin.dima.schubotz.fse.preprocess.ProcessData;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.client.LocalExecutor;

public class ProcessDataTest {
	@Test
	public void TestProcessData() throws Exception {
		try {
			String inputFilename = "file://" + getClass().getClassLoader().getResources("test10000.xml").nextElement().getPath();
			System.out.println("ProcessData testing on: " + inputFilename);
			String queryFile = "file://" + getClass().getClassLoader().getResources("fQuery.xml").nextElement().getPath();
			String keywordDocsFilename = "file://" + getClass().getClassLoader().getResources("keywordDocsMap.csv").nextElement().getPath();
			String latexDocsFilename = "file://" + getClass().getClassLoader().getResources("latexDocsMap.csv").nextElement().getPath();
	        String numDocsFilename = "file://" + getClass().getClassLoader().getResources("numDocs.txt").nextElement().getPath();
			//DEBUG file IO
//			keywordDocsFilename = "file:///home/jjl4/keywordDocsMap.csv";
//			latexDocsFilename = "file:///home/jjl4/latexDocsMap.csv";
//			numDocsFilename = "file:///home/jjl4/numDocs.txt";
			
			ProcessData.parseArg(new String[]{"16",
											  inputFilename,
											  queryFile,
											  keywordDocsFilename + Math.random() * Integer.MAX_VALUE,
											  latexDocsFilename + Math.random() * Integer.MAX_VALUE,
											  numDocsFilename});
			ProcessData.ConfigurePlan();
	        ExecutionEnvironment env = ProcessData.getExecutionEnvironment();
	        Plan plan = env.createProgramPlan();
	        LocalExecutor.execute(plan);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
