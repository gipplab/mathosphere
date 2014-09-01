package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.mappers.preprocess.DataPreprocess;
import de.tuberlin.dima.schubotz.fse.mappers.preprocess.FieldCountPreprocess;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.CSVHelper;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.aggregation.Aggregations;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.functions.ReduceFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import org.apache.commons.cli.Option;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;


public class RawToPreprocessed extends Algorithm {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(RawToPreprocessed.class);
    private static final String STR_SEPARATOR = MainProgram.STR_SEPARATOR;
    private static final Pattern WORD_SPLIT = MainProgram.WORD_SPLIT;

    @Override
    public Collection<Option> getOptionsAsIterable() {
        //No options
        return Collections.emptyList();
    }

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        final DataSet<RawDataTuple> querySet = data.getQuerySet();
        final DataSet<RawDataTuple> dataSet = data.getDataSet();

        //Redirect standard output to string to log number of documents, then reset StdOut
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));

            //Count total number of documents and output
            dataSet.map(new MapFunction<RawDataTuple, Integer>() {
                @Override
                public Integer map(RawDataTuple in) {
                    return 1;
                }
            }).reduce(new ReduceFunction<Integer>() {
                @Override
                public Integer reduce(Integer in1, Integer in2) {
                    return in1.intValue() + in2.intValue();
                }
            }).print();
            LOG.info("The number of documents is: " + baos.toString());
        } finally {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        }

        //Process all data
        final DataSet<DataTuple> preprocessedData = dataSet.flatMap(new DataPreprocess(
                MainProgram.WORD_SPLIT, MainProgram.STR_SEPARATOR));

        final DataSet<DataTuple> preprocessedQueries = querySet.flatMap(new DataPreprocess(
                MainProgram.WORD_SPLIT, MainProgram.STR_SEPARATOR));


        //Count up latex and keyword tokens
        final DataSet<Tuple2<String, Integer>> latexCounts = preprocessedData.flatMap(
                new FieldCountPreprocess(DataTuple.fields.latex.ordinal(), STR_SEPARATOR, WORD_SPLIT))
                .withBroadcastSet(preprocessedQueries, "Queries")
                .groupBy(0) //group on String
                .aggregate(Aggregations.SUM, 1); //aggregate on Integer

        final DataSet<Tuple2<String, Integer>> keywordCounts = preprocessedData.flatMap(
                new FieldCountPreprocess(DataTuple.fields.keywords.ordinal(), STR_SEPARATOR, WORD_SPLIT))
                .withBroadcastSet(preprocessedQueries, "Queries")
                .groupBy(0) //group on String
                .aggregate(Aggregations.SUM, 1); //aggregate on Integer

        //Output!
        String outputDir = Settings.getProperty(SettingNames.OUTPUT_DIR);
        if (!outputDir.endsWith("/")) {
            outputDir += "/";
        }
        CSVHelper.outputCSV(preprocessedData, outputDir + "preprocessedData.csv");
        CSVHelper.outputCSV(preprocessedQueries, outputDir + "preprocessedQueries.csv");
        CSVHelper.outputCSV(latexCounts, outputDir + "latexCounts.csv");
        CSVHelper.outputCSV(keywordCounts, outputDir + "keywordCounts.csv");
    }
}
