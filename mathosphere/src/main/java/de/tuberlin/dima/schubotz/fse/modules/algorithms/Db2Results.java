package de.tuberlin.dima.schubotz.fse.modules.algorithms;


import de.tuberlin.dima.schubotz.fse.mappers.QueryDepthMatch;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.types.DatabaseTuple;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;

import java.util.Collection;

public class Db2Results extends SimpleDbAlorithm {

    private static final Options MainOptions = new Options();

    static {
        //Load command line options here

    }

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return MainOptions.getOptions();
    }

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        DataSet<DatabaseTuple> input = data.getDatabaseTupleDataSet();
        //final FlatMapOperator<DatabaseTuple, Tuple3<Integer, String, String>> strict = input.flatMap(new ToStrict());
        //setSql("insert ignore into formulae_strict (fId, stricCmml,  abstractCD  ) values (?,?,?)");
        //strict.output( getOutput() );
        input.flatMap(new QueryDepthMatch() )
    }
}
