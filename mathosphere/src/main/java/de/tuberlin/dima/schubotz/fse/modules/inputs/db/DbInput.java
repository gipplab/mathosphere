package de.tuberlin.dima.schubotz.fse.modules.inputs.db;

import de.tuberlin.dima.schubotz.fse.mappers.cleaners.Cleaner;
import de.tuberlin.dima.schubotz.fse.mappers.cleaners.QueryCleaner;
import de.tuberlin.dima.schubotz.fse.mappers.preprocess.extractCMML;
import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.types.DatabaseTuple;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.typeutils.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.core.fs.Path;

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
    private static final Option QUERY_FILE = new Option(
            SettingNames.QUERY_FILE.getLetter(), SettingNames.QUERY_FILE.toString(),
            true, "Path to query file.");
    private static final Options options = new Options();
    private static final Options MainOptions = new Options();
    private final Cleaner queryCleaner;
	static {
		//Load command line options here
		PASSWORD.setRequired(true);
		PASSWORD.setArgName("password");
		MainOptions.addOption(PASSWORD);
        QUERY_FILE.setRequired(true);
        QUERY_FILE.setArgName("/path/to/queries");
        options.addOption(QUERY_FILE);
	}

    public DbInput() {
        this.queryCleaner = new QueryCleaner();
    }
	/**
	 * Gets options for command line.
	 *
	 * @return options
	 */
	public Collection<Option> getOptionsAsIterable () {
		return options.getOptions();
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
		data.setDatabaseTupleDataSet(
                env.createInput(JDBCInputFormat.buildJDBCInputFormat()
                                .setDrivername(DRIVERNAME)
                                .setDBUrl(DBURL)
                                .setUsername(USER)
                                .setPassword(PW)
                                .setQuery("SELECT * FROM formulae_name natural join formulae_fulltext")
                                .finish(),
                        new TupleTypeInfo(DatabaseTuple.class, STRING_TYPE_INFO, STRING_TYPE_INFO, INT_TYPE_INFO, SHORT_TYPE_INFO, STRING_TYPE_INFO)
                )
        );
        final TextInputFormat inputQueries = new TextInputFormat(new Path(
                Settings.getProperty(SettingNames.QUERY_FILE)));
        inputQueries.setDelimiter(queryCleaner.getDelimiter());
        final DataSet<String> rawQueryText = new DataSource<>(env, inputQueries, BasicTypeInfo.STRING_TYPE_INFO);
        final FlatMapOperator<String, RawDataTuple> queries = rawQueryText.flatMap(queryCleaner);
        data.setcQuerySet(queries.flatMap(new extractCMML()));
	}
}
