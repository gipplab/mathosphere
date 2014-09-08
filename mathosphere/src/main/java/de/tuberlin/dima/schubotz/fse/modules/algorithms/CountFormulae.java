package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple2;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;


public class CountFormulae implements Algorithm  {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(CountFormulae.class);
	//TODO: Make this configurable as other settings too
	private final static String DBURL = "jdbc:mysql://localhost:3306/mathosphere";
	public static final String DRIVERNAME = "org.mariadb.jdbc.Driver";
	public static final String USER = "mathosphere";
	public static String PASSWORD ;

    @Override
    public Collection<Option> getOptionsAsIterable() {
        //No options
        return Collections.emptyList();
    }


    public void configure(ExecutionEnvironment env, DataStorage data) throws Exception {
        final DataSet<RawDataTuple> dataSet = data.getDataSet();

        //Process all data
	    FlatMapOperator<RawDataTuple, Tuple2<String, Integer>> preprocessedData = dataSet.flatMap( new de.tuberlin.dima.schubotz.fse.mappers.preprocess.CountFormulae() );

	    try {
		    PASSWORD = IOUtils.toString(   this.getClass().getClassLoader().getResourceAsStream( "password" ));
	    } catch ( IOException e ) {
		    LOG.fatal( "Failed to read password. Terminating", e );
		    throw new Exception(  "Failed to read password. Terminating" );
	    }
	    preprocessedData.output(
		    // build and configure OutputFormat
	    JDBCOutputFormat.buildJDBCOutputFormat()
		    .setDrivername( DRIVERNAME )
		    .setDBUrl( DBURL)
		    .setPassword( PASSWORD )
		    .setUsername( USER )
		    .setQuery("insert ignore into formulae_count (pageId, count ) values (?,?)")
		    .finish()
	    );

    }
}
