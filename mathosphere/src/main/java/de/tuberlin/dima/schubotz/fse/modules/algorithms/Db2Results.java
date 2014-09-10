package de.tuberlin.dima.schubotz.fse.modules.algorithms;


import de.tuberlin.dima.schubotz.fse.mappers.MainMapper;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.types.DatabaseResultTuple;
import de.tuberlin.dima.schubotz.fse.types.DatabaseTuple;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;
import org.apache.flink.api.java.operators.FlatMapOperator;

public class Db2Results extends SimpleDbAlorithm {

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
	    FlatMapOperator<DatabaseTuple, DatabaseResultTuple> resultTupleFlat;
	    resultTupleFlat = data.getDatabaseTupleDataSet().flatMap( new MainMapper() )
		    .withBroadcastSet( data.getcQuerySet(), "Queries" )
		    .withBroadcastSet( data.getVotes(), "Votes" );
	    resultTupleFlat.output( getOutput() );
//	    data.getVotes().writeAsCsv( "file:///tmp/__final/2" );
//	    data.getcQuerySet().writeAsCsv( "file:///tmp/__final/3" );
//	    data.getDatabaseTupleDataSet().writeAsCsv( "file:///tmp/__final/4" );
//	    resultTupleFlat.writeAsCsv( "file:///tmp/__final/1" );

    }

	@Override
	protected JDBCOutputFormat getOutput () {
		setSql( DatabaseResultTuple.SQL );
		return super.getOutput();
	}
}
