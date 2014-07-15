package de.tuberlin.dima.schubotz.wiki;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
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
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.FileSystem.WriteMode;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.util.Collector;

/**
 * Returns search results for NTCIR-11 2014 Wikipedia Subtask
 *
 */
public class WikiProgram {
	/**
	 * Main execution environment for Stratosphere
	 */
	static ExecutionEnvironment env;
	/**
	 * Root logger class. Leave all logging implementation up to 
	 * Stratosphere and its config files.
	 */
	private static final Log LOG = LogFactory.getLog(WikiProgram.class);
	/**
	 * Splitter for tokens
	 */
	public static final String STR_SPLIT = "<S>";
	/**
	 * Generates matches for words separated by whitespace
	 */
	public static final Pattern WORD_SPLIT = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
	/**
	 * HashMultiset for storing preprocessed data of latex token : count 
	 * of documents containing token
	 */
	public static HashMultiset<String> latexWikiMultiset;
	
	
	//SETTINGS
	/**
	 * The overall maximal results that can be returned per query.
	 */
	public final static int MaxResultsPerQuery = 1000;
	/**
	 * Runtag in output
	 */
	public static final String RUNTAG = "wiki_latex";
	
	
	//ARGUMENTS
	/**
	 * Stratosphere parallelism
	 */
	static int noSubTasks;
	/**
	 * Output path and filename
	 */
	static String output;
	/**
	 * Input path and filename for wikipedia files
	 */
	static String wikiInput;
	/**
	 * Input path and filename for wikiquery file
	 */
	static String wikiQueryInput;
	/**
	 * Input path and filename for preprocessed csv
	 */
	static String latexWikiMapInput;
	/**
	 * Total number of wikipedia files
	 */
	static int numWiki;
	/**
	 * Enable or disable low level debugging TODO clean this up (make it based on logger level?)
	 */
	static boolean debug;
	
	
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
			throw new Exception("numWiki not given as parameter or is not a number");
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
	@SuppressWarnings("serial")
	public static void ConfigurePlan() throws IOException {
		env = ExecutionEnvironment.getExecutionEnvironment();
		TextInputFormat formatWiki = new TextInputFormat(new Path(wikiInput));

		
		//Generate latexWikiMap from preprocessed files
		latexWikiMultiset = CSVMultisetHelper.csvToMultiset(latexWikiMapInput);
		
		
		formatWiki.setDelimiter("</page>"); //this will leave a null doc at the end and a useless doc at the beginning. also, each will be missing a </page>
		DataSet<String> rawWikiText = new DataSource<>( env, formatWiki, BasicTypeInfo.STRING_TYPE_INFO );
		
		//Clean up and format wikitext TODO no known better way
		DataSet<String> cleanWikiText = rawWikiText.flatMap(new FlatMapFunction<String, String>() {
			final String endDoc = System.getProperty("line.separator") + "</mediawiki"; //used to search for last document weirdness
			@Override
			public void flatMap(String in, Collector<String> out) throws Exception {
				//Check for edge cases created from stratosphere split
				if (in.startsWith("<mediawiki")) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Hit mediawiki header document.");
					}
					return;
				}else if (in.startsWith(endDoc)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Hit mediawiki end doc.");
					}
					return;
				}
				if (!in.endsWith("</page>")) {
					in += "</page>";
				}
				in = StringEscapeUtils.unescapeHtml(in); 
				out.collect(in);
			}
		});
		
		TextInputFormat formatQuery = new TextInputFormat(new Path(wikiQueryInput));
		formatQuery.setDelimiter("</topic>"); //this will leave a System.getProperty("line.separator")</topics> at the end as well as header info at the begin 
		DataSet<String> rawWikiQueryText = new DataSource<>(env, formatQuery, BasicTypeInfo.STRING_TYPE_INFO);
		
		//Clean up and format queries TODO no known better way of dealing with results of splits
		DataSet<String> cleanWikiQueryText = rawWikiQueryText.flatMap(new FlatMapFunction<String, String>() {
			final String endQuery = System.getProperty("line.separator") + "</topics>"; //used to search for last query weirdness
			@Override
			public void flatMap(String in, Collector<String> out) throws Exception {
				//Dealing with badly formatted html as a result of Stratosphere split
				if (in.trim().length() == 0 || in.startsWith(endQuery)) { 
					if (LOG.isWarnEnabled()) {
						LOG.warn("Corrupt query: " + in);
					}
					return;
				}
				if (in.startsWith("<?xml ")) {
					in += "</topic></topics>";
				}else if (!in.endsWith("</topic>")) {
					in += "</topic>";
				}
			}
		});
		
		
		DataSet<WikiQueryTuple> wikiQuerySet = cleanWikiQueryText.flatMap(new WikiQueryMapper(STR_SPLIT));
		DataSet<WikiTuple> wikiSet = cleanWikiText.flatMap(new WikiMapper(STR_SPLIT))
												.withBroadcastSet(wikiQuerySet, "Queries");
		
		DataSet<ResultTuple> matches = wikiSet.flatMap(new QueryWikiMatcher(STR_SPLIT, latexWikiMultiset, numWiki, debug))
									  .withBroadcastSet(wikiQuerySet, "Queries");
		
		DataSet<OutputSimpleTuple> outputTuples = matches//Group by queryid
				.groupBy(0)
				//Sort by score <queryid, docid, score>
				.sortGroup(2, Order.DESCENDING) 
				.reduceGroup(new OutputSimple(MaxResultsPerQuery,RUNTAG));	
		outputTuples.writeAsCsv(output,"\n"," ",WriteMode.OVERWRITE);
	}


}
