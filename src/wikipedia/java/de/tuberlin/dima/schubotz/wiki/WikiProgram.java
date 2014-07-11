package de.tuberlin.dima.schubotz.wiki;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Returns search results for NTCIR-11 2014 Wikipedia Subtask
 *
 */
public class WikiProgram {
	private static final Log LOG = LogFactory.getLog(WikiProgram.class);
	
	static int noSubTasks;
	static String wikiInput;
	static String wikiQueryInput;
	static String latexWikiMapOutput;
	static String numWikiOutput;
	static boolean debug;
	
	public static final String STR_SPLIT = "<S>";
	public static final Pattern WORD_SPLIT = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS); 
	
	
	protected static void parseArg(String[] args) throws Exception {
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

	public static void main(String[] args) {
		parseArgs(args);
		try {
			ConfigurePlan();
		} catch (Exception e) {
			LOG.fatal("IOException or URI exception", e);
		} finally {
			System.exit(1);
		}
	}
	public static ExecutionEnvironment getExecutionEnvironment() {
		return env;
	}
	public static void ConfigurePlan() throws IOException, URISyntaxException {
		env = ExecutionEnvironment.getExecutionEnvironment();

		TextInputFormat formatWiki = new TextInputFormat(new Path(wikiInput));
		//TODO make these splits nicer
		formatWiki.setDelimiter("</page>"); //this will leave a null doc at the end and a useless doc at the beginning. also, each will be missing a </page>
		DataSet<String> rawWikiText = new DataSource<>( env, formatWiki, BasicTypeInfo.STRING_TYPE_INFO );
		
		TextInputFormat formatQuery = new TextInputFormat(new Path(wikiQueryInput));
		formatQuery.setDelimiter("</topic>"); //this will leave a System.getProperty("line.separator")</topics> at the end as well as header info at the begin 
		DataSet<String> rawWikiQueryText = new DataSource<>(env, formatQuery, BasicTypeInfo.STRING_TYPE_INFO);
		
		DataSet<WikiQueryTuple> wikiQuerySet = rawWikiQueryText.flatMap(new WikiQueryMapper(STR_SPLIT));
		DataSet<WikiTuple> wikiSet = rawWikiText.flatMap(new WikiMapper(STR_SPLIT));

	}


}
