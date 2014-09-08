package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.fse.client.ClientConsole;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
@Ignore
public class ProcessDataIT {
	private Integer numDocs;
	private String inputFile;
	private String debugOutput;
	
	@Parameterized.Parameters
	public static Collection<Object[]> inputParam() {
		return Arrays.asList(new Object[][] {
			{9999,"test10000.xml","/home/jjl4/"} 
		});
	}
	@SuppressWarnings("hiding")
	public ProcessDataIT(Integer numDocs, String inputFile, String debugOutput) {
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
			String queryFile = "file://" + getClass().getClassLoader().getResources("de/tuberlin/dima/schubotz/fse/fQuery.xml").nextElement().getPath();
			if (!debugOutput.equals("")) {
				keywordDocsFilename = debugOutput + "keywordDocsMap.csv";
				latexDocsFilename = debugOutput + "latexDocsMap.csv";
				numDocsFilename = debugOutput + "numDocs.txt";
			} else {
				keywordDocsFilename = "file://" + getClass().getClassLoader().getResources("keywordDocsMap.csv").nextElement().getPath();
				latexDocsFilename = "file://" + getClass().getClassLoader().getResources("latexDocsMap.csv").nextElement().getPath();
		        numDocsFilename = "file://" + getClass().getClassLoader().getResources("numDocs.txt").nextElement().getPath();
			}
			ClientConsole.parseParameters( new String[]{"16",
				inputFilename,
				queryFile,
				keywordDocsFilename,
				latexDocsFilename,
				numDocsFilename} );

			//ProcessData.ConfigurePlan();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
//		try {
//	        ExecutionEnvironment env = ProcessData.getExecutionEnvironment();
//	        Plan plan = env.createProgramPlan();
//	        LocalExecutor.execute( plan );
//		} catch (Exception e) {
//			fail("Execution error.");
//			e.printStackTrace();
//			return;
//		}
//
//		//Check to make sure correct file output
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(new FileReader(new File(numDocsFilename)));
//			assertEquals(Integer.valueOf(br.readLine()),numDocs);
//			br = new BufferedReader(new FileReader(new File(keywordDocsFilename)));
//			assertNotNull(br.readLine());
//			br = new BufferedReader(new FileReader(new File(latexDocsFilename)));
//			assertNotNull(br.readLine());
//		} catch (FileNotFoundException e) {
//			fail("Files not outputted or given directory is incorrect.");
//			e.printStackTrace();
//		} finally {
//			br.close();
//		}
		
		
	}

}
