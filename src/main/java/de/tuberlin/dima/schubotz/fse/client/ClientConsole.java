package de.tuberlin.dima.schubotz.fse.client;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.modules.Module;
import de.tuberlin.dima.schubotz.fse.modules.algorithms.Algorithm;
import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;

/**
 * Created by jjl4 on 8/7/14.
 */
public class ClientConsole {
    private ClientConsole() {}
    private static void printHelp() {
        //TODO
        System.out.println("Usage: stratosphere run <this-jar-file> algorithm input [ARGUMENTS]");
        System.out.println("HELP GOES HERE");
    }

    /**
     * Parses command line arguments.
     * @param args args from command line
     * @return boolean true if parameters parsed correctly, false otherwise
     */

    public static boolean parseParameters(String[] args) {
        if (args.length < 1) {
            System.out.println("No algorithm specified.");
            printHelp();
            return false;
        } else if (args.length < 2) {
            System.out.println("No input specified.");
            return false;
        }
        final String algorithm = args[0];
        final String input = args[1];
        final String[] params = Arrays.copyOfRange(args, 2, args.length);
        try {
            final Algorithm algorithmClass = MainProgram.getModule(
                    algorithm, Algorithm.class);
            final Input inputClass = MainProgram.getModule(
                    input, Input.class);
            Settings.loadOptions(params, algorithmClass, inputClass);
        } catch (final IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println("Invalid algorithm specified.");
            printHelp();
            return false;
        } catch (final MissingArgumentException e) {
            System.out.println(e.getMessage());
            printHelp();
            return false;
        } catch (final ParseException e) {
            System.out.println(e.getMessage());
            printHelp();
            return false;
        }
        return true;
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
