package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.Collection;


public class CountFormulae implements Algorithm  {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(CountFormulae.class);
	//TODO: Make this configurable as other settings too
	private final static String DBURL = "jdbc:mysql://localhost:3306/mathosphere";
	public static final String DRIVERNAME = "org.mariadb.jdbc.Driver";
	public static final String USER = "mathosphere";
    private static final Option PASSWORD= new Option(
            SettingNames.PASSWORD.getLetter(), SettingNames.PASSWORD.toString(), true,
            "Password for mysql");
    private static final Options MainOptions = new Options();

    static {
        //Load command line options here
        PASSWORD.setRequired(true);
        PASSWORD.setArgName("password");
        MainOptions.addOption(PASSWORD);
    }
    @Override
    public Collection<Option> getOptionsAsIterable() {
        return MainOptions.getOptions();
    }



    public void configure(ExecutionEnvironment env, DataStorage data) throws Exception {
        final DataSet<RawDataTuple> dataSet = data.getDataSet();

        //Process all data
	    FlatMapOperator<RawDataTuple, Tuple2<String, Integer>> preprocessedData = dataSet.flatMap( new de.tuberlin.dima.schubotz.fse.mappers.preprocess.CountFormulae() );

        String PW = Settings.getProperty(SettingNames.PASSWORD);
        preprocessedData.output(
		    // build and configure OutputFormat
	    JDBCOutputFormat.buildJDBCOutputFormat()
		    .setDrivername( DRIVERNAME )
		    .setDBUrl( DBURL)
		    .setPassword( PW )
		    .setUsername( USER )
		    .setQuery("insert ignore into formulae_count (pageId, count ) values (?,?)")
		    .finish()
	    );

    }
}
