package de.tuberlin.dima.schubotz.fse.modules.inputs.db;

import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;

import java.util.Collection;

import static org.apache.flink.api.java.typeutils.BasicTypeInfo.*;

/**
 * Created by Moritz on 08.09.2014.
 */
public class DbInput implements Input {
	public static final String DRIVERNAME = "org.mariadb.jdbc.Driver";
	public static final String USER = "mathosphere";
	//TODO: Make this configurable as other settings too
	protected final static String DBURL = "jdbc:mysql://localhost:3306/mathosphere";
	protected static final Option PASSWORD = new Option(
		SettingNames.PASSWORD.getLetter(), SettingNames.PASSWORD.toString(), true,
		"Password for mysql");
	private static final Options MainOptions = new Options();

	static {
		//Load command line options here
		PASSWORD.setRequired(true);
		PASSWORD.setArgName("password");
		MainOptions.addOption(PASSWORD);
	}
	/**
	 * Gets options for command line.
	 *
	 * @return options
	 */
	@Override
	public Collection<Option> getOptionsAsIterable () {
		return MainOptions.getOptions();
	}

	/**
	 * Configures environment.
	 *
	 * @param env  ExecutionEnvironment
	 * @param data
	 */
	@Override
	public void configure (ExecutionEnvironment env, DataStorage data) throws Exception {
		String PW = Settings.getProperty( SettingNames.PASSWORD );
		data.setDataSet(
			env.createInput( JDBCInputFormat.buildJDBCInputFormat()
				.setDrivername( DRIVERNAME )
				.setDBUrl( DBURL )
				.setUsername( USER )
				.setPassword( PW )
				.setQuery( "SELECT * FROM formulae_name natural join formulae_fulltext where isEquation is null" )
				.finish(),
			new TupleTypeInfo( Tuple5.class, STRING_TYPE_INFO, STRING_TYPE_INFO, INT_TYPE_INFO, SHORT_TYPE_INFO, STRING_TYPE_INFO )
		)
		);
	}
}
