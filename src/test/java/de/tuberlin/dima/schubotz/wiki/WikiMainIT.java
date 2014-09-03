package de.tuberlin.dima.schubotz.wiki;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
@Ignore
public class WikiMainIT {
    /*
	private Integer numWiki;
	private String inputQuery;
    private String inputLatexWikiMap;
    private String inputTupleWikiMap;
	private String output;
	private Boolean debug;

	@Parameterized.Parameters
	public static Collection<Object[]> inputNumDocs() {
		return Arrays.asList(new Object[][] {
				{new Integer(30040),
                 "/home/jjl4/wikiProgramOutput.csv",
                 "de/tuberlin/dima/schubotz/wiki/wikiTrainingQuery.xml",
                 "de/tuberlin/dima/schubotz/wiki/mappers/wikiTrainingDump.expectedLatex.csv",
                 "de/tuberlin/dima/schubotz/wiki/mappers/wikiTrainingDump.expected.csv",
                 Boolean.valueOf(true)}
		});
	}
	@SuppressWarnings("hiding")
	public WikiMainIT(Integer numWiki,
                      String output,
                      String inputQuery,
                      String inputLatexWikiMap,
                      String inputTupleWikiMap,
                      Boolean debug) {

		this.numWiki = numWiki;
        this.output = output;
		this.inputQuery = inputQuery;
        this.inputLatexWikiMap = inputLatexWikiMap;
        this.inputTupleWikiMap = inputTupleWikiMap;
		this.debug = debug;
	}

	@Test
	public void TestLocalExecution() throws Exception {
		try {
			String wikiQueryInput = getClass().getClassLoader().getResource(inputQuery).getPath();
            String wikiLatexMapInput = getClass().getClassLoader().getResource(inputLatexWikiMap).getPath();
            String wikiTupleMapInput = getClass().getClassLoader().getResource(inputTupleWikiMap).getPath();
			WikiProgram.parseArgs(new String[] {"1",
											  output,
											  wikiQueryInput,
											  wikiLatexMapInput,
                                              wikiTupleMapInput,
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
			br = new BufferedReader(new FileReader(new File(new URI(output).getPath())));
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
	*/


}
