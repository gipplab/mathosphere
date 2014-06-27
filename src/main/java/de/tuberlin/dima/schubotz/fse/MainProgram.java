package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.common.operators.Order;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.operators.FlatMapOperator;
import eu.stratosphere.api.java.tuple.Tuple4;
import eu.stratosphere.api.java.tuple.Tuple7;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
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
			: "file:///mnt/ntcir-math/test-output/testout-" + System.currentTimeMillis() + ".xml");
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
		DataSet<Tuple4<String, Integer, Integer, StringValue>> sectionDataSet = rawArticleText.map( new TitleExtractionMapper() );


		//Set up querydataset
		TextInputFormat formatQueries = new TextInputFormat( new Path( queryInput ) );
		formatQueries.setDelimiter( "</topics>" ); //Do not split topics
		DataSet rawQueryText = new DataSource<>( env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO );
		DataSet<Query> queryDataSet = rawQueryText.flatMap( new QueryMapper() );

		FlatMapOperator<Tuple4<String, Integer, Integer, StringValue>, Tuple7<String, Integer, Integer, String, String, Double, StringValue>>
			mathHits = sectionDataSet.flatMap( new FormulaMapper() )
				.withBroadcastSet( queryDataSet, "Queries" );
		mathHits.groupBy( 3 ).sortGroup( 5, Order.DESCENDING ).reduceGroup( new FormulaReducer() );
		//mathHits.writeAsText( output );  /*
		mathHits.print(); //*/
		/* ReduceGroupOperator<HitTuple, Tuple2<String, String>>
			result = mathHits
			.groupBy(HitTuple.fields.id.ordinal())
			.sortGroup(HitTuple.fields.score.ordinal(), Order.DESCENDING)
			.reduceGroup(new SingleQueryOutput());

		mathHits.print( output );


		//Return all matches of query keywords in format SectionTuple, sort by score, reduce to f0, document
		//SingleQueryOutput outputs XML document as well

		//queryDataSet.print();
		//DataSet<SectionTuple> articleDataSet = rawArticleText.map( new SectionMapper() ).withBroadcastSet( queryDataSet, "Queries" );
		//ProjectOperator<SectionTuple, Tuple2<SectionNameTuple, explicitDataSet>> formulaMatches = articleDataSet.project( SectionTuple.fields.name.ordinal(),
	//		SectionTuple.fields.formulae.ordinal() ).types( SectionNameTuple.class, explicitDataSet.class );
		//Projection<SectionTuple> keyWordMatches = articleDataSet.project(SectionTuple.fields.name.ordinal(),
		//        SectionTuple.fields.keywords.ordinal());


		/*result = mathHits
                .groupBy(1)
        DataSet<SectionTuple> articleDataSet = rawArticleText.flatMap(new SectionMapper()).withBroadcastSet(queryDataSet, "Queries");
        articleDataSet.writeAsText(output);
       /* ReduceGroupOperator<HitTuple, Tuple2<String, String>> result = articleDataSet
                .groupBy(HitTuple.fields.id.ordinal())
                .sortGroup(HitTuple.fields.score.ordinal(), Order.DESCENDING)
                .reduceGroup(new SingleQueryOutput());
        result.writeAsText(output);
        
        
        /* Demo that produces document ID and num of formulae - uses old code
        DataSet<Tuple2<String, Integer>> articleDataSet = rawArticleText.flatMap(new ArticleMapper()).withBroadcastSet(queryDataSet, "Queries");
        articleDataSet.writeAsText(output);
        */


		//PHASE A: PLAIN TEXT SEARCH
		//Step 1. Loop through documents.


		//Step 2. Extract plain text

		//Step 3. Filter queries based on whether plain text contains ALL of their keywords


	}


}

