package de.tuberlin.dima.schubotz.fse.modules.inputs.preprocessed;

import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.utils.CSVHelper;
import eu.stratosphere.api.java.ExecutionEnvironment;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Collection;

/**
 * Inputs additional multisets
 */
public class PreprocessedInput implements Input {
    /**
     * Command line options to add
     */
	private static final Option LATEX_DOCS_MAP = new Option(
            SettingNames.LATEX_DOCS_MAP.getLetter(), SettingNames.LATEX_DOCS_MAP.toString(), true,
            "Path(s) to latexDocsMap csv file (required by 'main' algorithm).");
    private static final Option KEYWORD_DOCS_MAP = new Option(
            SettingNames.KEYWORD_DOCS_MAP.getLetter(), SettingNames.KEYWORD_DOCS_MAP.toString(), true,
            "Path(s) to keywordDocsMap csv file (required by 'main' algorithm).");
    private static final Option DATATUPLE_FILE = new Option(
            SettingNames.DATATUPLE_FILE.getLetter(), SettingNames.DATATUPLE_FILE.toString(), true,
            "Path to preprocessed data tuple file.");
    private static final Option QUERYTUPLE_FILE = new Option(
            SettingNames.QUERYTUPLE_FILE.getLetter(), SettingNames.QUERYTUPLE_FILE.toString(), true,
            "Path to preprocessed query tuple file.");

    private static final Options InputOptions = new Options();

    static {
        //Load command line options
        LATEX_DOCS_MAP.setArgName("/path/to/latexDocsMap");
        LATEX_DOCS_MAP.setRequired(true);
        KEYWORD_DOCS_MAP.setArgName("/path/to/keywordDocsMap");
        KEYWORD_DOCS_MAP.setRequired(true);
        DATATUPLE_FILE.setArgName("/path/to/dataTupleFile");
        DATATUPLE_FILE.setRequired(true);
        QUERYTUPLE_FILE.setArgName("/path/to/queryTupleFile");
        QUERYTUPLE_FILE.setRequired(true);

        InputOptions.addOption(LATEX_DOCS_MAP);
        InputOptions.addOption(KEYWORD_DOCS_MAP);
        InputOptions.addOption(DATATUPLE_FILE);
        InputOptions.addOption(QUERYTUPLE_FILE);
    }

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return InputOptions.getOptions();
    }

    /**
     * Configure the environment to load multisets
     * @param env ExecutionEnvironment
     */
    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {
        data.setDataTupleSet(
                CSVHelper.csvToTuple(env, DataTuple.class, Settings.getProperty(SettingNames.DATATUPLE_FILE)));
        data.setQueryTupleSet(
                CSVHelper.csvToTuple(env, DataTuple.class, Settings.getProperty(SettingNames.QUERYTUPLE_FILE)));
        data.setKeywordSet(CSVHelper.csvToMultiset(Settings.getProperty(SettingNames.KEYWORD_DOCS_MAP)));
        data.setLatexSet(CSVHelper.csvToMultiset(Settings.getProperty(SettingNames.LATEX_DOCS_MAP)));
    }
}
