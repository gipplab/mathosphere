package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.commons.cli.Option;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;


public class CountFormulae implements Algorithm  {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(CountFormulae.class);
    private static final String STR_SEPARATOR = MainProgram.STR_SEPARATOR;
    private static final Pattern WORD_SPLIT = MainProgram.WORD_SPLIT;

    @Override
    public Collection<Option> getOptionsAsIterable() {
        //No options
        return Collections.emptyList();
    }


    public void configure(ExecutionEnvironment env, DataStorage data) {
        final DataSet<RawDataTuple> dataSet = data.getDataSet();

        //Process all data
	    FlatMapOperator<RawDataTuple, Tuple2<String, Integer>> preprocessedData = dataSet.flatMap( new de.tuberlin.dima.schubotz.fse.mappers.preprocess.CountFormulae() );

        //Output dir generation
        String outputDir = Settings.getProperty(SettingNames.OUTPUT_DIR);
        if (!outputDir.endsWith("/")) {
            outputDir += "/";
        }
	    //TODO:what's about hdfs://
        if (!outputDir.startsWith("file://")) {
            outputDir = "file://" + outputDir;
        }
//	    preprocessedData.output(
//		    // build and configure OutputFormat
//		   JDBCOutputFormat.buildJDBCOutputFormat()
//			    .setDrivername("org.apache.derby.jdbc.EmbeddedDriver")
//			    .setDBUrl("jdbc:derby:memory:persons")
//			    .setQuery("insert into persons (name, age, height) values (?,?,?)")
//			    .finish()
//	    );
	    preprocessedData.writeAsCsv( outputDir +"/fcnt.csv" );

    }
}
