package de.tuberlin.dima.schubotz.wiki.preprocess;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tuberlin.dima.schubotz.wiki.WikiProgram;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiCleaner;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiQueryCleaner;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiQueryMapper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
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

public class ProcessWiki {
	private static final Log LOG = LogFactory.getLog(ProcessWiki.class);
	static int noSubTasks;
	static String wikiInput;
	static String wikiQueryInput;
	static String latexWikiMapOutput;
	static String numWikiOutput;
	static boolean debug;
	
	static ExecutionEnvironment env;
	
	public static String STR_SPLIT = WikiProgram.STR_SPLIT;
	public static Pattern WORD_SPLIT = WikiProgram.WORD_SPLIT;
	
	public static final String WIKI_SEPARATOR = WikiProgram.WIKI_SEPARATOR;
	public static final String QUERY_SEPARATOR = WikiProgram.QUERY_SEPARATOR;
	
	
	public static void parseArgs(String[] args) {
		noSubTasks = (args.length > 0 ? Integer.parseInt( args[0] )
			: 16);
		wikiInput = (args.length > 1 ? args[1]
			: "file:///mnt/ntcir-math/testdata/augmentedWikiDump.xml");
		wikiQueryInput = (args.length > 2 ? args[2]
			: "file:///mnt/ntcir-math/queries/wikiQuery.xml");
		latexWikiMapOutput = (args.length > 3 ? args[3]
			: "file:///mnt/ntcir-math/queries/latexWikiMap.csv");
		numWikiOutput = (args.length > 4 ? args[4]
			: "file:///mnt/ntcir-math/queries/numWiki.txt");
		debug = (args.length > 5 ? 
					(args[5].equals("debug") ? true : false)
				: false);
		
	}
	public static void main(String[] args) throws Exception {
		parseArgs(args);
		try {
			ConfigurePlan();
		} catch (IOException e) {
			LOG.fatal("IO exception", e);
		} finally {
			System.exit(1);
		}
		env.setDegreeOfParallelism(noSubTasks);
		env.execute("MathosphereWiki Process Data");
		System.exit(1);
	}
	public static ExecutionEnvironment getExecutionEnvironment() {
		return env;
	}
	public static void ConfigurePlan() throws IOException, URISyntaxException {
		env = ExecutionEnvironment.getExecutionEnvironment();
		
		TextInputFormat formatWiki = new TextInputFormat(new Path(wikiInput));
		formatWiki.setDelimiter(WIKI_SEPARATOR); //this will leave a null doc at the end and a useless doc at the beginning. also, each will be missing a </page>
		DataSet<String> rawWikiText = new DataSource<>( env, formatWiki, BasicTypeInfo.STRING_TYPE_INFO );
		DataSet<String> cleanWikiText = rawWikiText.flatMap(new WikiCleaner());		
		
		TextInputFormat formatQuery = new TextInputFormat(new Path(wikiQueryInput));
		formatQuery.setDelimiter(QUERY_SEPARATOR); //this will leave a System.getProperty("line.separator")</topics> at the end as well as header info at the begin 
		DataSet<String> rawWikiQueryText = new DataSource<>(env, formatQuery, BasicTypeInfo.STRING_TYPE_INFO);
		DataSet<String> cleanWikiQueryText = rawWikiQueryText.flatMap(new WikiQueryCleaner());
		
		DataSet<WikiQueryTuple> wikiQuerySet = cleanWikiQueryText.flatMap(new WikiQueryMapper(STR_SPLIT));
		
		DataSet<Tuple2<String,Integer>> latexWikiResults = cleanWikiText.flatMap(new LatexWikiMapper(STR_SPLIT))
																		.withBroadcastSet(wikiQuerySet, "Queries")
																		.groupBy(0) //group by latex
																		.aggregate(Aggregations.SUM,1); //sum counts
		
		//Count total number of documents and output
		cleanWikiText.map(new MapFunction<String,Integer>() {
			@Override
			public Integer map(String in) {
				return 1;
			}
		}).reduce(new ReduceFunction<Integer>() {
			@Override
			public Integer reduce(Integer in1, Integer in2) {
				return in1 + in2;
			}
		}).writeAsText(numWikiOutput,WriteMode.OVERWRITE);
		
		latexWikiResults.writeAsCsv(latexWikiMapOutput, "\n", " ", WriteMode.OVERWRITE); 
	}
}
