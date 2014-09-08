package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.mappers.preprocess.IsEquation;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple3;
//import org.intellij.lang.annotations.Language;


public class EquationOrFormula extends SimpleDbMapper  {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(EquationOrFormula.class);

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        final DataSet<RawDataTuple> dataSet = data.getDataSet();

        String        PW = Settings.getProperty(SettingNames.PASSWORD);

        FlatMapOperator<RawDataTuple, Tuple3<Boolean,String, String>> equations = dataSet.flatMap(new IsEquation());
        //@Language("SQL")
        String sql = "UPDATE formulae_name set isEquation = ? WHERE pageId = ? and formula_name = ? LIMIT 1";
        equations.output(
                JDBCOutputFormat.buildJDBCOutputFormat()
                        .setDrivername(DRIVERNAME)
                        .setDBUrl(DBURL)
                        .setPassword(PW)
                        .setUsername(USER)
                        .setQuery(sql)
                        .finish()
        );
    }
}
