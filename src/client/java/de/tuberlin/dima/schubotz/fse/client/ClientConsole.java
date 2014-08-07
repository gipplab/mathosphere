package de.tuberlin.dima.schubotz.fse.client;

import de.tuberlin.dima.schubotz.fse.Settings;
import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * Created by jjl4 on 8/7/14.
 */
public class ClientConsole {
    private final CommandLineParser parser;
    private Settings settings;

    private static final String ALGO_MAIN = "main";
    private static final String ALGO_PREPROCESS = "preprocess";

    private static final Options MainOptions = new Options();
    private static final Options PreprocessOptions = new Options();

    private static final Option INPUT_OPTION = new Option("i", "inputformat", true, "Input format for data files.\n" +
            "Currently supports 'arXiv' and 'wikipedia'.");
    private static final Option DATA_FILE = new Option("d", "datafile", true, "Path to data file.");
    private static final Option QUERY_FILE = new Option("q", "queryfile", true, "Path to query file.");
    private static final Option RUNTAG = new Option("r", "runtag", true, "Runtag name.");

    //TODO come up with better method of handling these files
    private static final Option LATEX_DOCS_MAP = new Option("l", "latexdocsmap", true,
            "Path(s) to latexDocsMap csv file (required by 'main' algorithm).");
    private static final Option KEYWORD_DOCS_MAP = new Option("k", "keyworddocsmap", true,
            "Path(s) to keywordDocsMap csv file (required by 'main' algorithm).");

    static {
        INPUT_OPTION.setRequired(true);
        INPUT_OPTION.setArgName("input-format");

        DATA_FILE.setRequired(true);
        DATA_FILE.setArgName("/path/to/data");

        QUERY_FILE.setRequired(true);
        QUERY_FILE.setArgName("/path/to/queries");

        RUNTAG.setRequired(true);
        RUNTAG.setArgName("runtag");

        LATEX_DOCS_MAP.setRequired(true);
        LATEX_DOCS_MAP.setArgName("/path/to/latexDocsMap");

        KEYWORD_DOCS_MAP.setRequired(true);
        KEYWORD_DOCS_MAP.setArgName("/path/to/keywordDocsMap");

        MainOptions.addOption(INPUT_OPTION);
        PreprocessOptions.addOption(INPUT_OPTION);
        MainOptions.addOption(DATA_FILE);
        PreprocessOptions.addOption(DATA_FILE);
        MainOptions.addOption(QUERY_FILE);
        PreprocessOptions.addOption(QUERY_FILE);
        MainOptions.addOption(RUNTAG);
        PreprocessOptions.addOption(RUNTAG);
        PreprocessOptions.addOption(LATEX_DOCS_MAP);
        PreprocessOptions.addOption(KEYWORD_DOCS_MAP);

    }


    private void printHelpForMain() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setLeftPadding(5);
        formatter.setSyntaxPrefix("'Main' algorithm options:");
        formatter.printHelp(" ", MainOptions);
    }

    private void printHelpForPreprocess() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setLeftPadding(5);
        formatter.setSyntaxPrefix("'Preprocess' algorithm options:");
        formatter.printHelp(" ", PreprocessOptions);
    }

    /**
     * Parse arguments for main algorithm.
     * @param params params
     * @return true if parsed, false otherwise
     */
    private boolean parseMain(String[] params) {
        CommandLine line;
        try {
            line = parser.parse(MainOptions, params, false);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelpForMain();
            return false;
        }


        if (line.hasOption(INPUT_OPTION.getOpt())) {
            Settings.setProperty("")

        }
    }

    /**
     * Parse arguments for preprocess algorithm
     * @param params params
     * @return true if parsed, false otherwise
     */
    private boolean parsePreprocess(String[] params) {
        CommandLine line;
        try {
            line = parser.parse(PreprocessOptions, params, false);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelpForPreprocess();
            return false;
        }
    }

    /**
     * Parses command line arguments
     * @param args args from command line
     * @param settings settings to add parameters to
     * @return true if parameters parsed correctly, false otherwise
     */
    public boolean parseParameters(String[] args, Settings settings) {
        this.settings = settings;
        if (args.length < 1) {
            System.out.println("No algorithm specified.");
            System.out.println("Usage: stratosphere run <this-jar-file> algorithm [ARGUMENTS]");
            printHelpForMain();
            printHelpForPreprocess();
        }
        String algorithm = args[0];
        String[] params = Arrays.copyOfRange(args, 1, args.length);
        if (algorithm.equals(ALGO_MAIN)) {
            this.settings.setProperty("algorithm", "main");
            return parseMain(params);
        } else if (algorithm.equals(ALGO_PREPROCESS)) {
            this.settings.setProperty("algorithm", "preprocess");
            return parsePreprocess(params);
        } else{
            System.out.println("Invalid algorithm specified.");
            System.out.println("Usage: stratosphere run <this-jar-file> algorithm [ARGUMENTS]");
            printHelpForMain();
            printHelpForPreprocess();
            return false;
        }
    }

    public ClientConsole() {
        parser = new PosixParser();
    }
}
