package de.tuberlin.dima.schubotz.fse;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.common.mappers.OutputSimple;
import de.tuberlin.dima.schubotz.common.types.OutputSimpleTuple;
import de.tuberlin.dima.schubotz.common.utils.CSVHelper;
import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;
import de.tuberlin.dima.schubotz.fse.mappers.*;
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


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

/**
 * Performs the queries for the NTCIR-Math11-Workshop 2014 fully automated.
 */

public class MainProgram {
	/**
	 * Main execution environment for Stratosphere.
	 */
	private static ExecutionEnvironment env;
	/**
	 * Log for this class. Leave all logging implementations up to
	 * Stratosphere and its config files.
	 */
	private static final SafeLogWrapper LOG = new SafeLogWrapper(MainProgram.class);
	/**
	 * Delimiter used in between Tex and Keyword tokens
	 */
	public static final String STR_SEPARATOR = "<S>";
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
	public static final int MAX_RESULTS_PER_QUERY = 1000;
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
	

    /**
     * @param args arguments to parse
     * @return Returns true if args parsed successfully, false if not.
     * @throws IOException {@link Settings#loadConfig(String)} may throw
     */
	protected static boolean parseArg (String[] args) throws IOException {
        if (args.length > 0) {
            Settings.loadConfig(args[0]);
            return true;
        } else {
            System.out.println("Usage: stratosphere run <jar-file> {/path/to/settings}");
            return false;
        }
	}

	public static void main (String[] args) throws Exception {
        final boolean parsed = parseArg(args);
        if (parsed) {
            configureEnv();
            configurePlan();
            env.execute("Mathosphere");
        }
	}
    /**
     * Configure ExecutionEnvironment
     */
    protected static void configureEnv() {
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.setDegreeOfParallelism(Integer.parseInt(Settings.getProperty("numSubTasks")));
    }

    /**
     * Configure ExecutioniEnvironment
     * @throws java.lang.NoSuchMethodException attempt to access method by reflection may fail
     * @throws java.lang.reflect.InvocationTargetException attempt to access method by reflection may fail
     * @throws java.lang.IllegalAccessException attempt to access method by reflection may fail
     */
    protected static void configurePlan() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        final String planName = Settings.getProperty("main");
        MainProgram.class.getClass().getMethod(planName).invoke(null);
    }

	/**
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("serial")
	protected static void configureMainPlan () throws XPathExpressionException, ParserConfigurationException, Exception {
		//Set of keywords and latex contained in document and queries, mapped to counts of how many documents contain each token
		keywordDocsMultiset = CSVHelper.csvToMultiset(Settings.getProperty("keywordDocsMapInput"));
		latexDocsMultiset = CSVHelper.csvToMultiset(Settings.getProperty("latexDocsMapInput"));
		
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
		DataSet<QueryTuple> queryDataSet = cleanQueryText.flatMap(new QueryMapper(WORD_SPLIT, STR_SEPARATOR));
		DataSet<SectionTuple> sectionDataSet = cleanArticleText.flatMap(new SectionMapper(WORD_SPLIT, STR_SEPARATOR, keywordDocsMultiset));
		
		
		//Compare LaTeX and keywords, score
		DataSet<ResultTuple> latexMatches = sectionDataSet.flatMap(new QuerySectionMatcher(STR_SEPARATOR, latexDocsMultiset, keywordDocsMultiset,
																						   numDocs, keywordDivide))
														  .withBroadcastSet(queryDataSet, "Queries");
		//Output
		DataSet<OutputSimpleTuple> outputTuples = latexMatches//Group by queryid
														.groupBy(0)
														//Sort by score <queryid, docid, score>
														.sortGroup(2, Order.DESCENDING) 
														.reduceGroup(new OutputSimple(MAX_RESULTS_PER_QUERY,RUNTAG));
		outputTuples.writeAsCsv(output,"\n"," ",WriteMode.OVERWRITE);

	}

    protected static void configureWikiPlan() {

    }
    protected static void configureProcessWikiPlan() {

    }
    protected static void configureProcessMainDocumentsPlan() {
    }
}

