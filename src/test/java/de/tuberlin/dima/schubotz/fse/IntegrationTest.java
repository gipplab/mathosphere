package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.client.LocalExecutor;
import org.junit.Test;

public class IntegrationTest {
    @Test
    public void TestLocalExecution() throws Exception {
    	try {
	        String inputFilename = "file://" + getClass().getClassLoader().getResources("test1000.xml").nextElement().getPath();
	        System.out.println("testing on: " + inputFilename);
	        String queryFile = "file://" + getClass().getClassLoader().getResources("fQuery.xml").nextElement().getPath();
	        String outputFilename = "file://" + getClass().getClassLoader().getResources("test.out").nextElement().getPath();
	        MainProgram.parseArg(new String[]{"16", inputFilename, queryFile, outputFilename + Math.random() * Integer.MAX_VALUE});
	        MainProgram.ConfigurePlan();
	        ExecutionEnvironment env = MainProgram.getExecutionEnvironment();
	        Plan plan = env.createProgramPlan();//rc.getPlan(inputFilename, outputFilename + Math.random() * Integer.MAX_VALUE, "1.5", "0");
	        LocalExecutor.execute(plan);
    	}catch (Exception e) {
    		System.out.println(e.getMessage());
    		e.printStackTrace();
    	}
    }
}
