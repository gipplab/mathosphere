package de.tuberlin.dima.schubotz.fse.algorithms;

import de.tuberlin.dima.schubotz.common.utils.CSVHelper;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiCleaner;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiQueryCleaner;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiQueryMapper;
import de.tuberlin.dima.schubotz.wiki.preprocess.ProcessLatexWikiMapper;
import de.tuberlin.dima.schubotz.wiki.preprocess.ProcessWikiMapper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
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

/**
 * Created by jjl4 on 8/7/14.
 */
public class ConfigureWikiAlgorithm implements Algorithm {
    static {
        //Load all settings
    }
    public void configure(ExecutionEnvironment env) {
        env = ExecutionEnvironment.getExecutionEnvironment();

        TextInputFormat formatWiki = new TextInputFormat(new Path(wikiInput));
        formatWiki.setDelimiter(WIKI_SEPARATOR); //this will leave a null doc at the end and a useless doc at the beginning. also, each will be missing a </page>
        DataSet<String> rawWikiText = new DataSource<>(env, formatWiki, BasicTypeInfo.STRING_TYPE_INFO);
        DataSet<String> cleanWikiText = rawWikiText.flatMap(new WikiCleaner());


        TextInputFormat formatQuery = new TextInputFormat(new Path(wikiQueryInput));
        formatQuery.setDelimiter(QUERY_SEPARATOR); //this will leave a System.getProperty("line.separator")</topics> at the end as well as header info at the begin
        DataSet<String> rawWikiQueryText = new DataSource<>(env, formatQuery, BasicTypeInfo.STRING_TYPE_INFO);
        DataSet<String> cleanWikiQueryText = rawWikiQueryText.flatMap(new WikiQueryCleaner());
        DataSet<WikiQueryTuple> wikiQuerySet = cleanWikiQueryText.flatMap(new WikiQueryMapper(STR_SPLIT));

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
