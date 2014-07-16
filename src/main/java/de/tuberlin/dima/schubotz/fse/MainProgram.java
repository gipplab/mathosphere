package de.tuberlin.dima.schubotz.fse;

import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

import de.tuberlin.dima.schubotz.common.mappers.OutputSimple;
import de.tuberlin.dima.schubotz.common.types.OutputSimpleTuple;
import de.tuberlin.dima.schubotz.common.utils.CSVMultisetHelper;
import de.tuberlin.dima.schubotz.fse.mappers.DocCleaner;
import de.tuberlin.dima.schubotz.fse.mappers.QueryCleaner;
import de.tuberlin.dima.schubotz.fse.mappers.QueryMapper;
import de.tuberlin.dima.schubotz.fse.mappers.QuerySectionMatcher;
import de.tuberlin.dima.schubotz.fse.mappers.SectionMapper;
import de.tuberlin.dima.schubotz.fse.types.QueryTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.fse.types.SectionTuple;
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
	 * Main execution environment for Stratosphere.
	 */
	static ExecutionEnvironment env;
	/**
	 * Log for this class. Leave all logging implementations up to
	 * Stratosphere and its config files.
	 */
	private static final Log LOG = LogFactory.getLog( MainProgram.class );
	/**
	 * Delimiter used in between Tex and Keyword tokens
	 */
	public static final String STR_SPLIT = "<S>";
	/**
	 * Pattern which will return word tokens
	 */
	public static final Pattern WORD_SPLIT = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
	/**
	 * HashMultiset of keywords : count of number of documents containing that keyword
	 */
	public static HashMultiset<String> keywordDocsMultiset;
	/**
	 * HashMultiset of latex tokens : count of number of documents containing that latex token
	 */
	public static HashMultiset<String> latexDocsMultiset;
	
	
	
	//SETTINGS
	/**
	 * The overall maximal results that can be returned per query.
	 */
	public final static int MaxResultsPerQuery = 1000;
	/**
	 * Tag on which to split documents.
	 */
	public static final String DOCUMENT_SEPARATOR = "</ARXIVFILESPLIT>";
	/**
	 * Tag on which to split queries.
	 */
	public static final String QUERY_SEPARATOR = "</topic>";
	/**
	 * Amount to deweight keywords by. Divide tfidf_keyword by this 
	 * to get final keyword score.
	 */
	public static double keywordDivide = 6.36; 
	/**
	 * Runtag ID
	 */
	public static final String RUNTAG = "fse_LATEX";
	
	
	
	// ARGUMENTS
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
	 * The Output file with the calculated results
	 */
	static String output;
	/**
	 * Input file of map of keywords to number of documents that contain that keyword
	 */
	static String keywordDocsMapInput;
	/**
	 * Input file of map of latex tokens to number of documents that contain that token 
	 */
	static String latexDocsMapInput;
	/**
	 * Total number of documents
	 */
	public static int numDocs = 0;
	/**
	 * Whether or not to do low level debugging (TODO make it based on logger level?)
	 */
	public static boolean debug;

	
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
			throw new Exception("numDocs not given or is not a number!");
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
	@SuppressWarnings("serial")
	protected static void ConfigurePlan () throws XPathExpressionException, ParserConfigurationException, Exception {
		env = ExecutionEnvironment.getExecutionEnvironment();
		
		//Set of keywords and latex contained in document and queries, mapped to counts of how many documents contain each token
		keywordDocsMultiset = CSVMultisetHelper.csvToMultiset(keywordDocsMapInput);
		latexDocsMultiset = CSVMultisetHelper.csvToMultiset(latexDocsMapInput);
		
		TextInputFormat format = new TextInputFormat(new Path(docsInput));
		format.setDelimiter(DOCUMENT_SEPARATOR);
		DataSet<String> rawArticleText = new DataSource<>(env, format, BasicTypeInfo.STRING_TYPE_INFO);
		DataSet<String> cleanArticleText = rawArticleText.flatMap(new DocCleaner());
		
		TextInputFormat formatQueries = new TextInputFormat(new Path(queryInput));
		formatQueries.setDelimiter(QUERY_SEPARATOR); 
		DataSet<String> rawQueryText = new DataSource<>(env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO);
		DataSet<String> cleanQueryText = rawQueryText.flatMap(new QueryCleaner());
		
		// TODO IMPLEMENT ADDITIONAL SCORING METHODS 
		
		//Extract LaTeX and keywords 
		DataSet<QueryTuple> queryDataSet = cleanQueryText.flatMap(new QueryMapper(WORD_SPLIT, STR_SPLIT));
		DataSet<SectionTuple> sectionDataSet = cleanArticleText.flatMap(new SectionMapper(WORD_SPLIT, STR_SPLIT, keywordDocsMultiset));
		
		
		//Compare LaTeX and keywords, score
		DataSet<ResultTuple> latexMatches = sectionDataSet.flatMap(new QuerySectionMatcher(STR_SPLIT, latexDocsMultiset, keywordDocsMultiset,
																						   numDocs, keywordDivide, debug))
														  .withBroadcastSet(queryDataSet, "Queries"); 
		
		
		//Output
		DataSet<OutputSimpleTuple> outputTuples = latexMatches//Group by queryid
														.groupBy(0)
														//Sort by score <queryid, docid, score>
														.sortGroup(2, Order.DESCENDING) 
														.reduceGroup(new OutputSimple(MaxResultsPerQuery,RUNTAG));			 
		outputTuples.writeAsCsv(output,"\n"," ",WriteMode.OVERWRITE);

	}
}

