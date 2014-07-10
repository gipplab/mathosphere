package de.tuberlin.dima.schubotz.fse.preprocess;

import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.mappers.QueryMapper;
import de.tuberlin.dima.schubotz.fse.types.QueryTuple;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.aggregation.Aggregations;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.functions.ReduceFunction;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.FileSystem.WriteMode;
import eu.stratosphere.core.fs.Path;

public class ProcessData {
	static int noSubTasks;
	static String docsInput;
	static String queryInput;
	static String keywordDocsMapOutput;
	static String latexDocsMapOutput;
	static String numDocsOutput;
	
	public static String STR_SPLIT = MainProgram.STR_SPLIT;
	public static Pattern WORD_SPLIT = MainProgram.WORD_SPLIT;
	
	public static final String DOCUMENT_SEPARATOR = "</ARXIVFILESPLIT>";
	
	static ExecutionEnvironment env;
	
	/**
	 * @global noSubTasks
	 * @global docsInput
	 * @global queryInput
	 * @global keywordDocsMapOutput
	 * @global latexDocsMapOutput
	 */
	public static void parseArg (String[] args) {
		// parse job parameters
		noSubTasks = (args.length > 0 ? Integer.parseInt( args[0] )
			: 16);
		docsInput = (args.length > 1 ? args[1]
			: "file:///mnt/ntcir-math/testdata/test10000.xml");
		queryInput = (args.length > 2 ? args[2]
			: "file:///mnt/ntcir-math/queries/fQuery.xml");
		keywordDocsMapOutput = (args.length > 3 ? args[3]
			: "file:///mnt/ntcir-math/queries/keywordDocsMap.csv");
		latexDocsMapOutput = (args.length > 4 ? args[4]
			: "file:///mnt/ntcir-math/queries/latexDocsMap.csv");
		numDocsOutput = (args.length > 5 ? args[5]
			: "file:///mnt/ntcir-math/queries/numDocs.txt");
	}
	
	public static void main (String[] args) throws Exception {
		parseArg( args );
		ConfigurePlan();
		env.setDegreeOfParallelism( noSubTasks );
		env.execute( "Mathosphere Process Data" );
		System.exit(1);
	}
	
	public static ExecutionEnvironment getExecutionEnvironment () throws Exception {
		return env;
	}
	
	/**
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 */
	public static void ConfigurePlan () throws XPathExpressionException, ParserConfigurationException, Exception {
		env = ExecutionEnvironment.getExecutionEnvironment();	
		//Set up articleDataSet
		TextInputFormat format = new TextInputFormat( new Path( docsInput ) );
		format.setDelimiter( DOCUMENT_SEPARATOR ); 
		DataSet<String> rawArticleText = new DataSource<>( env, format, BasicTypeInfo.STRING_TYPE_INFO );
		
		//Set up querydataset
		TextInputFormat formatQueries = new TextInputFormat( new Path( queryInput ) );
		formatQueries.setDelimiter( "</topic>" ); 
		DataSet<String> rawQueryText = new DataSource<>( env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO ); 
		
		DataSet<QueryTuple> queryDataSet = rawQueryText.flatMap(new QueryMapper(WORD_SPLIT, STR_SPLIT));
		
		//Construct map of query keyword: number of docs containing that keyword
		DataSet<Tuple2<String,Integer>> keyDocResults = rawArticleText.flatMap(new KeywordDocMapper(WORD_SPLIT, STR_SPLIT))
																.withBroadcastSet(queryDataSet, "Queries")
																.groupBy(0) //group by keyword
																.aggregate(Aggregations.SUM, 1); //aggregate based on field 1
		
		//Construct map of latex tokens: number of docs containing that token
		DataSet<Tuple2<String,Integer>> latexDocResults = rawArticleText.flatMap(new LatexDocMapper(STR_SPLIT))
																.withBroadcastSet(queryDataSet, "Queries")
																.groupBy(0) //group by keyword
																.aggregate(Aggregations.SUM,1); //aggregate based on field 1
		
		//Count total number of documents and output
		rawArticleText.map(new MapFunction<String,Integer>() {
			@Override
			public Integer map(String in) {
				return (in.trim().length()) > 0 ? 1 : 0;
			}
		}).reduce(new ReduceFunction<Integer>() {
			@Override
			public Integer reduce(Integer in1, Integer in2) {
				return in1 + in2;
			}
		}).writeAsText(numDocsOutput,WriteMode.OVERWRITE);
		
		keyDocResults.writeAsCsv(keywordDocsMapOutput,"\n"," ",WriteMode.OVERWRITE);
		latexDocResults.writeAsCsv(latexDocsMapOutput,"\n"," ",WriteMode.OVERWRITE);
		
		
	}


}
