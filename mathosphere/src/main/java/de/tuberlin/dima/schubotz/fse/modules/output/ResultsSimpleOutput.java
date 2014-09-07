package de.tuberlin.dima.schubotz.fse.modules.output;

import de.tuberlin.dima.schubotz.fse.mappers.OutputSimple;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.types.OutputSimpleTuple;
import de.tuberlin.dima.schubotz.fse.utils.CSVHelper;
import org.apache.commons.cli.Option;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;

import java.util.Collection;
import java.util.Collections;

/**
 * Outputs to CSV in simple format
 */
public class ResultsSimpleOutput implements Output {
    private static final int MAX_RESULTS_PER_QUERY = 1000;

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        //Output
        final DataSet<OutputSimpleTuple> outputTuples = data.getResultSet()
                .groupBy(0) //Group by queryid
                .sortGroup(2, Order.DESCENDING) //Sort by score <queryid, docid, score>
                .reduceGroup(new OutputSimple(MAX_RESULTS_PER_QUERY, Settings.getProperty(SettingNames.RUNTAG)));
        CSVHelper.outputCSV(outputTuples, Settings.getProperty(SettingNames.OUTPUT_DIR) + "/simpleOutput.csv");
    }

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return Collections.emptyList();
    }
}
