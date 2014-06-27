package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.common.operators.Order;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.io.TextOutputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.operators.FlatMapOperator;
import eu.stratosphere.api.java.operators.SortedGrouping;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.api.java.tuple.Tuple4;
import eu.stratosphere.api.java.tuple.Tuple6;
import eu.stratosphere.api.java.tuple.Tuple7;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.FileSystem.WriteMode;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.types.StringValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import java.util.HashMap;
import java.util.Map;

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
	
	public static final String TEX_SPLIT = "<S>";
	
	public static final String RUNTAG_LATEX = "fse_LATEX";
	
	public static Map<String, String> TeXQueries = new HashMap<String, String>();
	// set up execution environment
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

	protected static void ConfigurePlan () throws XPathExpressionException, ParserConfigurationException {
		env = ExecutionEnvironment.getExecutionEnvironment();

		//Set up articleDataSet
		TextInputFormat format = new TextInputFormat( new Path( docsInput ) );
		format.setDelimiter( DOCUMENT_SEPARATOR );
		//rawArticleText format: data set of strings, delimited by <ARXIFFILESPLIT>
		DataSet<String> rawArticleText = new DataSource<>( env, format, BasicTypeInfo.STRING_TYPE_INFO );
		
		
		//Set up querydataset
		TextInputFormat formatQueries = new TextInputFormat( new Path( queryInput ) );
		formatQueries.setDelimiter( "</topic>" ); 
		DataSet rawQueryText = new DataSource<>( env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO ); //TODO: something is blank - maybe in how it splits
		
		
		/**TODO IMPLEMENT ADDITIONAL SCORING METHODS */
		
		
		/**PHASE A: extract LaTeX, tokenize*/
		//returns doc/query id, tokenized latex split by <S>
		DataSet<Tuple2<String,String>> queryDataSet = null;
		DataSet<Tuple2<String,String>> sectionDataSet = null; 
		try {
			queryDataSet = rawQueryText.flatMap(new QueryLatexMapper());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			sectionDataSet = rawArticleText.flatMap(new SectionLatexMapper());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		
		/**PHASE B: compare LaTeX, get number of hits, group by ID, sort by score, limit to 20 per query, genreate rank and runtag */
		//TODO fix null result as a result of split? maybe keep this as it makes sure score is at least 1
		//returns queryid,1,docid,,rank,numhits,runtag
		//TODO optimize so that queryid,docid are seen by Stratosphere as constants
		DataSet<Tuple6<String,Integer,String,Integer,Integer,String>> latexMatches = null;
		try {
			 latexMatches= sectionDataSet.flatMap(new QuerySectionMatcher())
					 					  .withBroadcastSet(queryDataSet, "Queries")
										  //Group by queryid
										  .groupBy(0)
										  //Sort by score: result: queryid, docid, numhits
										  .sortGroup(2, Order.DESCENDING)
										  //Limit to 20 per query (20x50=1000), add rank/score
										  .reduceGroup(new OutputSimple());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
			
		
		/**PHASE C: output*/
		try {
			//latexMatches.writeAsCsv("/home/jjl4/testRegExp.txt","\n"," ",WriteMode.OVERWRITE); //DEBUG 
			latexMatches.writeAsCsv(output,"\n"," ",WriteMode.OVERWRITE);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		

		

	}


}

