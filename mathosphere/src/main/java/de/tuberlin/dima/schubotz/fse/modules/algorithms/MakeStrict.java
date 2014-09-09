package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.mappers.dbMapper.ToStrict;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.types.DatabaseTuple;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * Created by mas9 on 9/9/14.
 */
public class MakeStrict extends SimpleDbAlorithm {

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) throws Exception {
        DataSet<DatabaseTuple> input = data.getDatabaseTupleDataSet();
        final FlatMapOperator<DatabaseTuple, Tuple3<Integer, String, String>> strict = input.flatMap(new ToStrict());
        setSql("insert ignore into formulae_strict (fId, stricCmml,  abstractCD  ) values (?,?,?)");
        strict.output( getOutput() );

    }
}
