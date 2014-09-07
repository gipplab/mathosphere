package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.mappers.preprocess.DataPreprocess;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.CSVHelper;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.commons.cli.Option;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.core.fs.FileSystem;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;


public class EquationOrFormula implements Algorithm  {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(EquationOrFormula.class);
    private static final String STR_SEPARATOR = MainProgram.STR_SEPARATOR;
    private static final Pattern WORD_SPLIT = MainProgram.WORD_SPLIT;

    @Override
    public Collection<Option> getOptionsAsIterable() {
        //No options
        return Collections.emptyList();
    }

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        final DataSet<RawDataTuple> dataSet = data.getDataSet();

        //Process all data
        final DataSet<DataTuple> preprocessedData = dataSet.flatMap(new DataPreprocess(
                MainProgram.WORD_SPLIT, MainProgram.STR_SEPARATOR));

        //Output dir generation
        String outputDir = Settings.getProperty(SettingNames.OUTPUT_DIR);
        if (!outputDir.endsWith("/")) {
            outputDir += "/";
        }
        if (!outputDir.startsWith("file://")) {
            outputDir = "file://" + outputDir;
        }

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
        }).writeAsText(outputDir + "numDocs.csv", FileSystem.WriteMode.OVERWRITE);

        CSVHelper.outputCSV(preprocessedData, outputDir + "preprocessedData.csv");

    }
}
