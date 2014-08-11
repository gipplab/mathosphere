package de.tuberlin.dima.schubotz.fse.client;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.modules.algorithms.Algorithm;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * Created by jjl4 on 8/7/14.
 */
public class ClientConsole {
    private static void printHelp() {
        //TODO
        System.out.println("Usage: stratosphere run <this-jar-file> algorithm [ARGUMENTS]");
        System.out.println("HELP GOES HERE");
    }

    /**
     * Parses command line arguments. All required arguments are guaranteed
     * to exist if this method does not throw an exception.
     * @param args args from command line
     * @return algorithm if parameters parsed correctly, null otherwise
     */

    public static Algorithm parseParameters(String[] args) {
        if (args.length < 1) {
            System.out.println("No algorithm specified.");
            printHelp();
            return null;
        }
        final String algorithm = args[0];
        final String[] params = Arrays.copyOfRange(args, 1, args.length);
        try {
            final Class returnedClass = MainProgram.getSubClass(
                    algorithm, Algorithm.class);
            final Algorithm algorithmClass = Algorithm.class.cast(returnedClass);
            Settings.loadOptions(params, algorithmClass);
            return algorithmClass;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println("Invalid algorithm specified.");
            printHelp();
            return null;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelp();
            return null;
        }
    }
        /*
    private static void printHelpForMain() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setLeftPadding(5);
        formatter.setSyntaxPrefix("'Main' algorithm options:");
        formatter.printHelp(" ", Settings.getMainOptions());
    }

    private static void printHelpForPreprocess() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setLeftPadding(5);
        formatter.setSyntaxPrefix("'Preprocess' algorithm options:");
        formatter.printHelp(" ", Settings.getPreprocessOptions());
    }
    */

    /**
     * Parse arguments for main algorithm.
     * @param params params
     * @return true if parsed, false otherwise
     */
    /*
    private boolean parseMain(String[] params) {
        try {
            final CommandLine line = parser.parse(MainOptions, params, false);
            Settings.parse(line);
        } catch (final ParseException e) {
            System.out.println(e.getMessage());
            printHelpForMain();
            return false;
        }

    }

    /**
     * Parse arguments for preprocess algorithm
     * @param params params
     * @return true if parsed, false otherwise
     */
    /*
    private boolean parsePreprocess(String[] params) {
        try {
            final CommandLine line = parser.parse(PreprocessOptions, params, false);
            Settings.setProperty(INPUT_OPTION);
            Settings.setProperty()
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelpForPreprocess();
            return false;
        }
    }
    */
}
