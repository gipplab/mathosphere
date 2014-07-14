package de.tuberlin.dima.schubotz.fse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

import de.tuberlin.dima.schubotz.fse.mappers.OutputSimple;
import de.tuberlin.dima.schubotz.fse.mappers.QueryMapper;
import de.tuberlin.dima.schubotz.fse.mappers.QuerySectionMatcher;
import de.tuberlin.dima.schubotz.fse.mappers.SectionMapper;
import de.tuberlin.dima.schubotz.fse.types.OutputSimpleTuple;
import de.tuberlin.dima.schubotz.fse.types.QueryTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.fse.types.SectionTuple;
import de.tuberlin.dima.schubotz.utils.CSVMultisetHelper;
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
	private static boolean debug; 
	/**
	 * Delimiter used in between Tex and Keyword tokens
	 */
	public static final String STR_SPLIT = "<S>";
	/**
	 * Delimiter for words in document/queries
	 */
	public static final Pattern WORD_SPLIT = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS); 
	/**
	 * Runtag ID
	 */
	public static final String RUNTAG = "fse_LATEX";
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
	 * Input file of map of keywords to number of documents that contain that keyword
	 */
	static String keywordDocsMapInput;
	static HashMultiset<String> keywordDocsMultiset;
	/**
	 * Input file of map of latex tokens to number of documents that contain that token 
	 */
	static String latexDocsMapInput;
	static HashMultiset<String> latexDocsMultiset;
	/**
	 * Total number of documents
	 */
	static Integer numDocs = 0;
	static double keywordDivide = 6.36; //TODO Amount to de-weight keywords by: tfidf_keyword / keywordDivide 

	
	protected static void parseArg (String[] args) throws Exception {
		// parse job parameters
		noSubTasks = (args.length > 0 ? Integer.parseInt( args[0] )
			: 16);
		docsInput = (args.length > 1 ? args[1]
			: "file:///mnt/ntcir-math/testdata/test10000.xml");
		queryInput = (args.length > 2 ? args[2]
			: "file:///mnt/ntcir-math/queries/fQuery.xml");
		output = (args.length > 3 ? args[3]
			: "file:///mnt/ntcir-math/test-output/LATEXtestout-" + System.currentTimeMillis() + ".csv");
		keywordDocsMapInput = (args.length > 4 ? args[4]
			: "file:///mnt/ntcir-math/queries/keywordDocsMap.csv");
		latexDocsMapInput = (args.length > 5 ? args[5]
			: "file:///mnt/ntcir-math/queries/latexDocsMap.csv");
		if (args.length > 6) {
			numDocs = Integer.valueOf(args[6]);
		} else {
			throw new Exception("numDocs not given!");
		}
		debug = (args.length > 7 ? (args[7].equals("debug") ? true : false) : false);
	}

	public static void main (String[] args) throws Exception {
		try {
			parseArg( args );
		} catch (Exception e) {
			LOG.fatal("Arguments incorrect.", e);
			e.printStackTrace();
			System.exit(0);
		}
		try {
			ConfigurePlan();
			env.setDegreeOfParallelism( noSubTasks );
			env.execute( "Mathosphere" );
		} catch (Exception e) {
			System.out.println("Aborting!");
			System.exit(0);
		}
		System.exit(1);
	}

	public static ExecutionEnvironment getExecutionEnvironment () throws Exception {
		return env;
	}

	/**
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 */
	protected static void ConfigurePlan () throws XPathExpressionException, ParserConfigurationException, Exception {
		env = ExecutionEnvironment.getExecutionEnvironment();

		//Generate keywordDocsMap and latexDocsMap from preprocessed generated files
		keywordDocsMultiset = CSVMultisetHelper.csvToMultiset(keywordDocsMapInput);
		latexDocsMultiset = CSVMultisetHelper.csvToMultiset(latexDocsMapInput);
		
		
		//Set up articleDataSet
		TextInputFormat format = new TextInputFormat( new Path( docsInput ) );
		format.setDelimiter( DOCUMENT_SEPARATOR );
		DataSet<String> rawArticleText = new DataSource<>( env, format, BasicTypeInfo.STRING_TYPE_INFO );
		
		
		//Set up querydataset
		TextInputFormat formatQueries = new TextInputFormat( new Path( queryInput ) );
		formatQueries.setDelimiter( "</topic>" ); 
		DataSet<String> rawQueryText = new DataSource<>( env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO ); 
		
		
		// TODO IMPLEMENT ADDITIONAL SCORING METHODS 
		
		//PHASE A: extract LaTeX and keywords 
		DataSet<QueryTuple> queryDataSet = rawQueryText.flatMap(new QueryMapper(WORD_SPLIT, STR_SPLIT));
		DataSet<SectionTuple> sectionDataSet = rawArticleText.flatMap(new SectionMapper(WORD_SPLIT, STR_SPLIT, keywordDocsMultiset));
		
		
		//PHASE B: compare LaTeX and keywords, score
		DataSet<ResultTuple> latexMatches = sectionDataSet.flatMap(new QuerySectionMatcher(STR_SPLIT, latexDocsMultiset, keywordDocsMultiset, numDocs, debug))
														  .withBroadcastSet(queryDataSet, "Queries"); 
		
		
		//PHASE C: output
		DataSet<OutputSimpleTuple> outputTuples = latexMatches//Group by queryid
														.groupBy(0)
														//Sort by score <queryid, docid, score>
														.sortGroup(2, Order.DESCENDING) 
														.reduceGroup(new OutputSimple(QUERYLIMIT,RUNTAG));			 
		outputTuples.writeAsCsv(output,"\n"," ",WriteMode.OVERWRITE);

	}
}

