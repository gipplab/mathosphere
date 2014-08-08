package de.tuberlin.dima.schubotz.fse.modules.inputs;

import de.tuberlin.dima.schubotz.fse.mappers.DocCleaner;
import de.tuberlin.dima.schubotz.fse.mappers.QueryCleaner;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.Path;
import org.apache.commons.cli.Option;

import java.util.Collection;
import java.util.Collections;

/**
 * Configures input for ARXIV data input format
 */
public class ConfigureArxivInput implements Input {
    private static final String DOCUMENT_SEPARATOR = "</ARXIVFILESPLIT>";
	private static final String QUERY_SEPARATOR = "</topic>";

    //Add any options here

    static {
        //Initialize command line options here
    }

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return Collections.emptyList();
    }

    @Override
    public void configure(ExecutionEnvironment env) {
        final TextInputFormat format = new TextInputFormat(new Path(
                Settings.getProperty(SettingNames.DATA_FILE)));
        format.setDelimiter(DOCUMENT_SEPARATOR);
        final DataSet<String> rawArticleText = new DataSource<>(env, format, BasicTypeInfo.STRING_TYPE_INFO);
        final DataSet<String> cleanArticleText = rawArticleText.flatMap(new DocCleaner());


        final TextInputFormat formatQueries = new TextInputFormat(new Path(
                Settings.getProperty(SettingNames.QUERY_FILE)));
        formatQueries.setDelimiter(QUERY_SEPARATOR);
        final DataSet<String> rawQueryText = new DataSource<>(env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO);
        final DataSet<String> cleanQueryText = rawQueryText.flatMap(new QueryCleaner());

    }

}
