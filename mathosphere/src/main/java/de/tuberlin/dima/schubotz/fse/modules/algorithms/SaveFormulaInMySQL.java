package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.mappers.dbMapper.FormulaToDB;
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


public class SaveFormulaInMySQL extends SimpleDbAlorithm {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(SaveFormulaInMySQL.class);

    public SaveFormulaInMySQL() {
        super();
    }

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        final DataSet<RawDataTuple> dataSet = data.getDataSet();

        String PW = Settings.getProperty(SettingNames.PASSWORD);

        FlatMapOperator<RawDataTuple, Tuple3<String,String, String>> equations = dataSet.flatMap(new FormulaToDB());
        //@Language("SQL")
        String sql = "insert ignore into formulae_fulltext (pageId, formula_name,  value  ) values (?,?,?)";
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
