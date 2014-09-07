package de.tuberlin.dima.schubotz.fse.modules.algorithms;


import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.api.java.ExecutionEnvironment;

import java.util.Collection;
import java.util.regex.Pattern;

/*
 * Configure MainPlan. For ArXiv dataset.
 * This plan takes in preprocessed files, matches de.tuberlin.dima.schubotz.fse.wiki and query documents, and then scores them.
 * Created by jjl4 on 8/7/14.
*/

public class PreprocessedToResults implements Algorithm {
    //Add any commandline options here
    private static final Option NUM_DOCS= new Option(
            SettingNames.NUM_DOC.getLetter(), SettingNames.NUM_DOC.toString(), true,
            "Number of documents");
    private static final Options MainOptions = new Options();

    static {
        //Load command line options here
        NUM_DOCS.setRequired(true);
        NUM_DOCS.setArgName("numdocs");
        MainOptions.addOption(NUM_DOCS);
    }

    private static final Pattern WORD_SPLIT = MainProgram.WORD_SPLIT;
    private static final String STR_SEPARATOR = MainProgram.STR_SEPARATOR;

    /*
     * Amount to deweight keywords by. Divide tfidf_keyword by this
     * to get final keyword score.
    */
    private static final double KEYWORD_DIVIDE = 6.36;

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return MainOptions.getOptions();
    }

    /**
     * Configures this algorithm.
     * Must be configured after settings are loaded, otherwise will throw exception.
     * @param env ExecutionEnvironment */
    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        /*
        HashMultiset<String> latexMultiset = data.getLatexSet();
        DataSet<DataTuple> querySet = data.getQuerySet().flatMap(new QueryMapper(WORD_SPLIT, STR_SEPARATOR));


        /*WikiQueryMapper cleanWikiQueryText;
        DataSet<QueryTuple> wikiQuerySet = cleanWikiQueryText.flatMap(new WikiQueryMapper(STR_SPLIT));
		DataSet<ResultTuple> matches = wikiSet.flatMap(new QueryWikiMatcher(STR_SPLIT, latexWikiMultiset, numWiki))
									  .withBroadcastSet(wikiQuerySet, "Queries");

		DataSet<OutputSimpleTuple> outputTuples = matches//Group by queryid
				.groupBy(0)
				//Sort by score <queryid, docid, score>
				.sortGroup(2, Order.DESCENDING)
				.reduceGroup(new OutputSimple(MaxResultsPerQuery, RUNTAG));
		outputTuples.writeAsCsv(output,"\n"," ",WriteMode.OVERWRITE);


        //Extract LaTeX and keywords
        final DataSet<QueryTuple> queryDataSet = queryStrings.flatMap(new QueryMapper(WORD_SPLIT, STR_SEPARATOR));
        final DataSet<SectionTuple> sectionDataSet = dataStrings.flatMap(
                new SectionMapper(WORD_SPLIT, STR_SEPARATOR, keywordDocsMultiset));

        //Compare LaTeX and keywords, score
        final DataSet<ResultTuple> latexMatches = sectionDataSet.flatMap(
                new QuerySectionMatcher(STR_SEPARATOR, latexDocsMultiset, keywordDocsMultiset,
                        Integer.parseInt(Settings.getProperty(SettingNames.NUM_DOC)),
                        KEYWORD_DIVIDE))
                .withBroadcastSet(queryDataSet, "Queries");*/
    }
}
