package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import eu.stratosphere.api.java.ExecutionEnvironment;
import org.apache.commons.cli.Option;

import java.util.Collection;
import java.util.Collections;


public class RawToPreprocessed implements Algorithm {
    static {
        //Load all settings
    }

    @Override
    public Collection<Option> getOptionsAsIterable() {
        //No options
        return Collections.emptyList();
    }

    public void configure(ExecutionEnvironment env, DataStorage data) {
        /*env = ExecutionEnvironment.getExecutionEnvironment();
        WikiQueryCleaner rawWikiQueryText;
        DataSet<String> cleanWikiQueryText = rawWikiQueryText.flatMap(new WikiQueryCleaner());
        DataSet<QueryTuple> wikiQuerySet = cleanWikiQueryText.flatMap(new WikiQueryMapper(STR_SPLIT));

        DataSet<Tuple2<String, Integer>> latexWikiResults = cleanWikiText.flatMap(new ProcessLatexWikiMapper(STR_SPLIT))
                .withBroadcastSet(wikiQuerySet, "Queries")
                .groupBy(0) //group by latex
                .aggregate(Aggregations.SUM, 1); //sum counts

        DataSet<WikiTuple> wikiSet = cleanWikiText.flatMap(new ProcessWikiMapper(STR_SPLIT));

        //Count total number of documents and output
        cleanWikiText.map(new MapFunction<String, Integer>() {
            @Override
            public Integer map(String in) {
                return 1;
            }
        }).reduce(new ReduceFunction<Integer>() {
            @Override
            public Integer reduce(Integer in1, Integer in2) {
                return in1 + in2;
            }
        }).writeAsText(numWikiOutput, FileSystem.WriteMode.OVERWRITE);

        CSVHelper.outputCSV(latexWikiResults, latexWikiMapOutput);
        CSVHelper.outputCSV(wikiSet, tupleWikiMapOutput);

        latexWikiResults.writeAsCsv(latexWikiMapOutput, CSV_LINE_SEPARATOR, CSV_FIELD_SEPARATOR, FileSystem.WriteMode.OVERWRITE);
        wikiSet.writeAsCsv(tupleWikiMapOutput, CSV_LINE_SEPARATOR, CSV_FIELD_SEPARATOR, FileSystem.WriteMode.OVERWRITE);
*/
    }
}
