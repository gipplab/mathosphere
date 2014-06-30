package de.tuberlin.dima.schubotz.fse;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.api.common.operators.Order;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.FileSystem.WriteMode;
import eu.stratosphere.core.fs.Path;

/**
 * Performs the queries for the NTCIR-Math11-Workshop 2014 fully automated.
 */

public class MainProgram {
	/**
	 * The overall maximal results that can be returned per query.
	 */
	public final static int MaxResultsPerQuery = 3000;
	/**
	 * The Constant RECORD_WORD.
	 */
	public static final int RECORD_WORD = 0;
	/**
	 * The Constant RECORD_VARIABLE.
	 */
	public static final int RECORD_VARIABLE = 1;
	/**
	 * The Constant RECORD_MATCH.
	 */
	public static final int RECORD_MATCH = 2;
	public static final Map<String, String> QueryDesc = new HashMap<>();
	public static final String DOCUMENT_SEPARATOR = "</ARXIVFILESPLIT>";
	public static final String RECOD_TYPE = "RECORD_TYPE";
	/**
	 * The Constant LOG.
	 */
	private static final Log LOG = LogFactory.getLog( MainProgram.class );
	/**
	 * Delimiter used in between Tex and Keyword tokens
	 */
	public static final String STR_SPLIT = "<S>";
	/**
	 * Runtag ID
	 */
	public static final String RUNTAG_LATEX = "fse_LATEX";
	/**
	 * Limit of results per query
	 */
	public static final int QUERYLIMIT = 1000; 
	
	public static Map<String, String> TeXQueries = new HashMap<String, String>();
	
	static ExecutionEnvironment env;
	/**
	 * The number of parallel tasks to be executed
	 */
	static int noSubTasks;
	/**
	 * The Input XML-File that contains the document collection
	 */
	static String docsInput;
	/**
	 * The Input CSV-file with the human evaluation
	 */
	static String queryInput;
	/**
	 * The Output XML file with the calculated results
	 */
	static String output;
	/** 
	 * Score added per keyword hit
	 */
	static int KeywordScore = 10;
	/**
	 * Score added per plaintext hit
	 */
	static int plaintextScore = 1;
	/**
	 * Score added per latex token hit
	 */
	static int LatexScore = 1;

	
	protected static void parseArg (String[] args) {
		// parse job parameters
		noSubTasks = (args.length > 0 ? Integer.parseInt( args[0] )
			: 16);
		docsInput = (args.length > 1 ? args[1]
			: "file:///mnt/ntcir-math/testdata/test10000.xml");
		queryInput = (args.length > 2 ? args[2]
			: "file:///mnt/ntcir-math/queries/fQuery.xml");
		output = (args.length > 3 ? args[3]
			: "file:///mnt/ntcir-math/test-output/LATEXtestout-" + System.currentTimeMillis() + ".xml");
	}

	public static void main (String[] args) throws Exception {
		parseArg( args );
		ConfigurePlan();
		env.setDegreeOfParallelism( noSubTasks );
		env.execute( "Mathosphere" );
	}

	public static ExecutionEnvironment getExecutionEnvironment () throws Exception {
		return env;
	}

	/**
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 */
	protected static void ConfigurePlan () throws XPathExpressionException, ParserConfigurationException {
		env = ExecutionEnvironment.getExecutionEnvironment();

		//Set up articleDataSet
		TextInputFormat format = new TextInputFormat( new Path( docsInput ) );
		format.setDelimiter( DOCUMENT_SEPARATOR ); //TODO fix stratosphere split issues for Queries and Documents
		DataSet<String> rawArticleText = new DataSource<>( env, format, BasicTypeInfo.STRING_TYPE_INFO );
		
		
		//Set up querydataset
		TextInputFormat formatQueries = new TextInputFormat( new Path( queryInput ) );
		formatQueries.setDelimiter( "</topic>" ); 
		DataSet<String> rawQueryText = new DataSource<>( env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO ); 
		
		
		// TODO IMPLEMENT ADDITIONAL SCORING METHODS 
		
		
		/**PHASE A: extract LaTeX and keywords */
		//TODO extract plaintext from query as well?
		DataSet<QueryTuple> queryDataSet = rawQueryText.flatMap(new QueryMapper());
		DataSet<SectionTuple> sectionDataSet = rawArticleText.flatMap(new SectionMapper()); 
		
		
		queryDataSet.writeAsCsv("/home/jjl4/testRegExp.txt","\n",",",WriteMode.OVERWRITE); //DEBUG
/*		
		*//**PHASE B: compare LaTeX, get number of hits, group by ID, sort by score, limit to 1000 per query, generate rank and runtag *//*
		//TODO fix null result as a result of split? maybe keep this as it makes sure score is at least 1
		DataSet<ResultTuple> latexMatches = sectionDataSet.flatMap(new QuerySectionMatcher())
														  .withBroadcastSet(queryDataSet, "Queries"); 
		DataSet<OutputSimpleTuple> output = latexMatches
														//Group by queryid
														.groupBy(0)
														//Sort by score <queryid, docid, score>
														.sortGroup(2, Order.DESCENDING) 
														.reduceGroup(new OutputSimple());			
		
		*//**PHASE C: output*//*
		try {
			output.writeAsCsv("/home/jjl4/testRegExp.txt","\n"," ",WriteMode.OVERWRITE); //DEBUG 
			//latexMatches.writeAsCsv(output,"\n"," ",WriteMode.OVERWRITE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
*/
	}


}

