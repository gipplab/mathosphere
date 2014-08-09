package de.tuberlin.dima.schubotz.fse.modules.inputs.raw;

import de.tuberlin.dima.schubotz.fse.mappers.DocCleaner;
import de.tuberlin.dima.schubotz.fse.mappers.QueryCleaner;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.Path;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Collection;

/**
 * Configures input for ARXIV data input format
 */
public class ArxivRawInput implements RawInput {
    /**
     * TODO Consider moving these into command line options/settings properties
     */
    private static final String DOCUMENT_SEPARATOR = "</ARXIVFILESPLIT>";
	private static final String QUERY_SEPARATOR = "</topic>";

    //Add any options here
    private static final Option DATA_FILE = new Option(
            SettingNames.DATARAW_FILE.getLetter(), SettingNames.DATARAW_FILE.toString(),
            true, "Path to data file.");
    private static final Option QUERY_FILE = new Option(
            SettingNames.QUERY_FILE.getLetter(), SettingNames.QUERY_FILE.toString(),
            true, "Path to query file.");

    private static final Options options = new Options();

    static {
        //Initialize command line options here
        DATA_FILE.setRequired(true);
        DATA_FILE.setArgName("/path/to/data");

        QUERY_FILE.setRequired(true);
        QUERY_FILE.setArgName("/path/to/queries");

        options.addOption(DATA_FILE);
        options.addOption(QUERY_FILE);
    }

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return options.getOptions();
    }

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        final TextInputFormat formatQueries = new TextInputFormat(new Path(
                Settings.getProperty(SettingNames.QUERY_FILE)));
        formatQueries.setDelimiter(QUERY_SEPARATOR);
        final DataSet<String> rawQueryText = new DataSource<>(env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO);
        data.setQuerySet(rawQueryText.flatMap(new QueryCleaner()));

        final TextInputFormat format = new TextInputFormat(new Path(
                Settings.getProperty(SettingNames.DATARAW_FILE)));
        format.setDelimiter(DOCUMENT_SEPARATOR);
        final DataSet<String> rawArticleText = new DataSource<>(env, format, BasicTypeInfo.STRING_TYPE_INFO);
        data.setDataRawSet(rawArticleText.flatMap(new DocCleaner()));
    }

}
