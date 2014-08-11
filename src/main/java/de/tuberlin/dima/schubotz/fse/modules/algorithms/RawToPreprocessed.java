/*
package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import CSVHelper;
import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.types.QueryTuple;
import de.tuberlin.dima.schubotz.fse.types.WikiTuple;
import WikiCleaner;
import WikiQueryCleaner;
import WikiQueryMapper;
import ProcessLatexWikiMapper;
import ProcessWikiMapper;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.aggregation.Aggregations;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.functions.ReduceFunction;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.FileSystem;
import eu.stratosphere.core.fs.Path;
import org.apache.commons.cli.Option;

import java.util.Collection;
import java.util.Collections;

*/
/**
 * Created by jjl4 on 8/7/14.
 *//*

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
        env = ExecutionEnvironment.getExecutionEnvironment();
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

    }
}
*/
