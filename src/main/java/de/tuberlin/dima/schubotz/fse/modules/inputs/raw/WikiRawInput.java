package de.tuberlin.dima.schubotz.fse.modules.inputs.raw;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.wiki.mappers.WikiQueryCleaner;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.Path;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by jjl4 on 8/7/14.
 */
public class WikiRawInput implements RawInput {
    /**
     * TODO Consider moving these into command line options/settings properties
     */
    public static final String DOCUMENT_SEPARATOR = "</page>";
    public static final String QUERY_SEPARATOR = "</topic>";

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

    private static final String WIKI_SEPARATOR = "" ;

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return Collections.emptyList();
    }

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        final TextInputFormat formatQuery = new TextInputFormat(new Path(Settings.getProperty(SettingNames.QUERY_FILE)));
        //this will leave a System.getProperty("line.separator")</topics> at the end as well as header info at the begin
        formatQuery.setDelimiter(QUERY_SEPARATOR);
        final DataSet<String> rawWikiQueryText = new DataSource<>(env, formatQuery, BasicTypeInfo.STRING_TYPE_INFO);
        data.setQuerySet(rawWikiQueryText.flatMap(new WikiQueryCleaner()));
        /*TextInputFormat formatWiki = new TextInputFormat(new Path(wikiInput));
        //this will leave a null doc at the end and a useless doc at the beginning. also, each will be missing a </page>
        formatWiki.setDelimiter(WIKI_SEPARATOR);
        DataSet<String> rawWikiText = new DataSource<>(env, formatWiki, BasicTypeInfo.STRING_TYPE_INFO);
        DataSet<String> cleanWikiText = rawWikiText.flatMap(new WikiCleaner());
*/
    }
}
