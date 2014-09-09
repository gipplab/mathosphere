package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;

import java.util.Collection;

/**
 * Created by mas9 on 9/8/14.
 */
public abstract class SimpleDbMapper implements Algorithm {
    public static final String DRIVERNAME = "org.mariadb.jdbc.Driver";
    public static final String USER = "mathosphere";
    //TODO: Make this configurable as other settings too
    protected final static String DBURL = "jdbc:mysql://localhost:3306/mathosphere";
    protected static final Option PASSWORD = new Option(
            SettingNames.PASSWORD.getLetter(), SettingNames.PASSWORD.toString(), true,
            "Password for mysql");
    protected static final Options MainOptions = new Options();
    static {
        //Load command line options here
        PASSWORD.setRequired(true);
        PASSWORD.setArgName("password");
        MainOptions.addOption(PASSWORD);
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    private String sql;

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return MainOptions.getOptions();
    }

    protected JDBCOutputFormat getOutput() {
        String PW = Settings.getProperty(SettingNames.PASSWORD);
        return JDBCOutputFormat.buildJDBCOutputFormat()
                .setDrivername(DRIVERNAME)
                .setDBUrl(DBURL)
                .setPassword(PW)
                .setUsername(USER)
                .setQuery(sql)
                .finish();
    }
}
