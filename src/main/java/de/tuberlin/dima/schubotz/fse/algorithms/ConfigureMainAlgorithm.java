package de.tuberlin.dima.schubotz.fse.algorithms;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.common.mappers.OutputSimple;
import de.tuberlin.dima.schubotz.common.types.OutputSimpleTuple;
import de.tuberlin.dima.schubotz.common.utils.CSVHelper;
import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.Settings;
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
import eu.stratosphere.core.fs.Path;

import java.util.regex.Pattern;

/**
 * Configure MainPlan. For ArXiv dataset.
 * This plan takes in preprocessed files, matches wiki and query documents, and then scores them.
 * Created by jjl4 on 8/7/14.
 */
public class ConfigureMainAlgorithm implements Algorithm {
    private static final Pattern WORD_SPLIT = MainProgram.WORD_SPLIT;
    private static final String STR_SEPARATOR = MainProgram.STR_SEPARATOR;

    private static final int MAX_RESULTS_PER_QUERY; //= 1000;
    private static final String DATASET;
    private static final String QUERYSET;
    private static final int NUM_DOCS;
	private static final String DOCUMENT_SEPARATOR; //= "</ARXIVFILESPLIT>";
	private static final String QUERY_SEPARATOR; //= "</topic>";
    private static final String OUTPUT_PATH;
    private static final String RUNTAG;
    private static final String KEYWORD_DOCS_MAP_INPUT;
    private static final String LATEX_DOCS_MAP_INPUT;
    /**
     * Amount to deweight keywords by. Divide tfidf_keyword by this
     * to get final keyword score.
     */
	private static final double KEYWORD_DIVIDE;// = 6.36;

    static {
        //Load all settings
        MAX_RESULTS_PER_QUERY = Integer.parseInt(Settings.getProperty("maxResultsPerQuery"));
        DATASET = Settings.getProperty("dataSet");
        QUERYSET = Settings.getProperty("querySet");
        NUM_DOCS = Integer.parseInt(Settings.getProperty("numDocs"));
        KEYWORD_DOCS_MAP_INPUT = Settings.getProperty("keywordDocsMapInput");
        LATEX_DOCS_MAP_INPUT = Settings.getProperty("latexDocsMapInput");
        DOCUMENT_SEPARATOR = Settings.getProperty("documentSeparator");
        QUERY_SEPARATOR = Settings.getProperty("querySeparator");
        KEYWORD_DIVIDE = Double.parseDouble(Settings.getProperty("keywordDivide"));
        OUTPUT_PATH = Settings.getProperty("output");
        RUNTAG = Settings.getProperty("runtag");
    }


    @Override
    public void configure(ExecutionEnvironment env) {
        //Set of keywords and latex contained in document and queries, mapped to counts of how many documents contain each token
        final HashMultiset<String> keywordDocsMultiset = CSVHelper.csvToMultiset(KEYWORD_DOCS_MAP_INPUT);
        final HashMultiset<String> latexDocsMultiset = CSVHelper.csvToMultiset(LATEX_DOCS_MAP_INPUT);


        final TextInputFormat format = new TextInputFormat(new Path(DATASET));
        format.setDelimiter(DOCUMENT_SEPARATOR);
        final DataSet<String> rawArticleText = new DataSource<>(env, format, BasicTypeInfo.STRING_TYPE_INFO);
        final DataSet<String> cleanArticleText = rawArticleText.flatMap(new DocCleaner());


        final TextInputFormat formatQueries = new TextInputFormat(new Path(QUERYSET));
        formatQueries.setDelimiter(QUERY_SEPARATOR);
        final DataSet<String> rawQueryText = new DataSource<>(env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO);
        final DataSet<String> cleanQueryText = rawQueryText.flatMap(new QueryCleaner());

        //Extract LaTeX and keywords
        final DataSet<QueryTuple> queryDataSet = cleanQueryText.flatMap(new QueryMapper(WORD_SPLIT, STR_SEPARATOR));
        final DataSet<SectionTuple> sectionDataSet = cleanArticleText.flatMap(
                new SectionMapper(WORD_SPLIT, STR_SEPARATOR, keywordDocsMultiset));


        //Compare LaTeX and keywords, score
        final DataSet<ResultTuple> latexMatches = sectionDataSet.flatMap(
                new QuerySectionMatcher(STR_SEPARATOR, latexDocsMultiset, keywordDocsMultiset,
                        NUM_DOCS, KEYWORD_DIVIDE))
                .withBroadcastSet(queryDataSet, "Queries");
        //Output
        final DataSet<OutputSimpleTuple> outputTuples = latexMatches//Group by queryid
                .groupBy(0)
                        //Sort by score <queryid, docid, score>
                .sortGroup(2, Order.DESCENDING)
                .reduceGroup(new OutputSimple(MAX_RESULTS_PER_QUERY,RUNTAG));

        CSVHelper.outputCSV(outputTuples, OUTPUT_PATH);
    }
}
