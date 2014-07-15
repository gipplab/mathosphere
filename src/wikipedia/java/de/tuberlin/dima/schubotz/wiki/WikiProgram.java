package de.tuberlin.dima.schubotz.wiki;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

import de.tuberlin.dima.schubotz.fse.mappers.OutputSimple;
import de.tuberlin.dima.schubotz.fse.types.OutputSimpleTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.utils.CSVMultisetHelper;
import de.tuberlin.dima.schubotz.wiki.mappers.QueryWikiMatcher;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiMapper;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiQueryMapper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.common.operators.Order;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.FileSystem.WriteMode;
import eu.stratosphere.core.fs.Path;


/**
 * Returns search results for NTCIR-11 2014 Wikipedia Subtask
 *
 */
public class WikiProgram {
	private static final Log LOG = LogFactory.getLog(WikiProgram.class);
	
	static ExecutionEnvironment env;
	
	static int noSubTasks;
	static String output;
	static String wikiInput;
	static String wikiQueryInput;
	static String latexWikiMapInput;
	static HashMultiset<String> latexWikiMultiset;
	static int numWiki;
	static boolean debug;
	/**
	 *  Limit of results per query
	 */
	public static final int QUERYLIMIT = 1000;
	/**
	 * Runtag in output
	 */
	public static final String RUNTAG = "wiki_latex";
	
	public static final String STR_SPLIT = "<S>";
	public static final Pattern WORD_SPLIT = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS); 
	
	
	protected static void parseArgs(String[] args) throws Exception {
		noSubTasks = (args.length > 0 ? Integer.parseInt( args[0] )
				: 16);
		output = (args.length > 1 ? args[1]
				: "file://mnt/ntcir-math/test-output/WikiProgramOUT-" + System.currentTimeMillis() + ".csv");
		wikiInput = (args.length > 2 ? args[2]
				: "file:///mnt/ntcir-math/testdata/augmentedWikiDump.xml");
		wikiQueryInput = (args.length > 3 ? args[3]
				: "file:///mnt/ntcir-math/queries/wikiQuery.xml");
		latexWikiMapInput = (args.length > 4 ? args[4]
				: "file:///mnt/ntcir-math/queries/latexWikiMap.csv");
		try {
			numWiki = Integer.valueOf(args[5]);
		} catch (Exception e) {
			throw new Exception("numWiki not given as parameter");
		}
		debug = (args.length > 6 ? 
				(args[5].equals("debug") ? true : false)
				: false);
			
	}

	public static void main(String[] args) {
		try {
			parseArgs(args);
			ConfigurePlan();
			env.setDegreeOfParallelism(noSubTasks);
			env.execute("MathoWikiSphere");
		} catch (Exception e) {
			LOG.fatal("Env WikiProgram execution exception", e);
			e.printStackTrace();
			System.exit(0);
		}
		System.exit(1);
	}
	public static ExecutionEnvironment getExecutionEnvironment() {
		return env;
	}
	public static void ConfigurePlan() throws IOException {
		env = ExecutionEnvironment.getExecutionEnvironment();
		TextInputFormat formatWiki = new TextInputFormat(new Path(wikiInput));

		//Generate latexWikiMap from preprocessed files
		latexWikiMultiset = CSVMultisetHelper.csvToMultiset(latexWikiMapInput);
		
		//TODO make these splits nicer
		formatWiki.setDelimiter("</page>"); //this will leave a null doc at the end and a useless doc at the beginning. also, each will be missing a </page>
		DataSet<String> rawWikiText = new DataSource<>( env, formatWiki, BasicTypeInfo.STRING_TYPE_INFO );
		
		TextInputFormat formatQuery = new TextInputFormat(new Path(wikiQueryInput));
		formatQuery.setDelimiter("</topic>"); //this will leave a System.getProperty("line.separator")</topics> at the end as well as header info at the begin 
		DataSet<String> rawWikiQueryText = new DataSource<>(env, formatQuery, BasicTypeInfo.STRING_TYPE_INFO);
		
		DataSet<WikiQueryTuple> wikiQuerySet = rawWikiQueryText.flatMap(new WikiQueryMapper(STR_SPLIT));
		DataSet<WikiTuple> wikiSet = rawWikiText.flatMap(new WikiMapper(STR_SPLIT))
												.withBroadcastSet(wikiQuerySet, "Queries");
		
		DataSet<ResultTuple> matches = wikiSet.flatMap(new QueryWikiMatcher(STR_SPLIT, latexWikiMultiset, numWiki, debug))
									  .withBroadcastSet(wikiQuerySet, "Queries");
		
		DataSet<OutputSimpleTuple> outputTuples = matches//Group by queryid
				.groupBy(0)
				//Sort by score <queryid, docid, score>
				.sortGroup(2, Order.DESCENDING) 
				.reduceGroup(new OutputSimple(QUERYLIMIT,RUNTAG));	
		outputTuples.writeAsCsv(output,"\n"," ",WriteMode.OVERWRITE);
	}


}
