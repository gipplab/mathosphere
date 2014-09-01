package de.tuberlin.dima.schubotz.fse.settings;

import de.tuberlin.dima.schubotz.fse.modules.Module;
import de.tuberlin.dima.schubotz.fse.modules.algorithms.Algorithm;
import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.commons.cli.*;

import java.io.Serializable;
import java.util.Properties;

/**
 * Parses config files and command lines.
 * Is statically initialized with default properties.
 * Used to generate Properties objects
 */
public class Settings implements Serializable {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(Settings.class);
    private static final Options GeneralOptions = new Options();
    private static final Options AllOptions = new Options();
    /**
     * Used to store all properties except for algorithm
     */
    private static final Properties CURRENT_PROPERTIES = new Properties();

    /**
     * List of all command line options.
     * The names should be set in the enum {@link Settings#}
     */
    private static final Option RUNTAG = new Option(
            SettingNames.RUNTAG.getLetter(), SettingNames.RUNTAG.toString(),
            true, "Runtag name.");
    private static final Option NUM_SUB_TASKS = new Option(
            SettingNames.NUM_SUB_TASKS.getLetter(), SettingNames.NUM_SUB_TASKS.toString(),
            true, "Parellelization");
    private static final Option OUTPUT_DIR = new Option(
            SettingNames.OUTPUT_DIR.getLetter(), SettingNames.OUTPUT_DIR.toString(),
            true, "Directory for output files.");
    //TODO currently algorithms require specific OUTPUT_OPTION
    /*
    private static final Option OUTPUT_OPTION = new Option(
            SettingNames.OUTPUT_OPTION.getLetter(), SettingNames.OUTPUT_OPTION.toString(),
            true, "Output class to run.");*/

    static {
        RUNTAG.setRequired(true);
        RUNTAG.setArgName("runtag");

        NUM_SUB_TASKS.setRequired(true);
        NUM_SUB_TASKS.setArgName("num-subtasks");

        OUTPUT_DIR.setRequired(true);
        OUTPUT_DIR.setArgName("/path/to/output");

        GeneralOptions.addOption(RUNTAG);
        GeneralOptions.addOption(NUM_SUB_TASKS);
        GeneralOptions.addOption(OUTPUT_DIR);

        for (final Object option : GeneralOptions.getOptions()) {
            if (option instanceof Option) {
                AllOptions.addOption((Option) option);
            }
        }
    }

    private Settings() {
    }

    /**
     * Load options from arguments for specific algorithm.
     * @param args arguments
     * @param algorithm algorithm
     * @throws ParseException commandlineparser may fail or options may be missing
     */
    public static void loadOptions(String[] args, Algorithm algorithm, Input input)
            throws MissingArgumentException, ParseException {
        //Add algorithm specific options to be loaded
        for (final Option option : algorithm.getOptionsAsIterable()) {
            AllOptions.addOption(option);
        }
        setProperty(SettingNames.ALGORITHM, algorithm.getClass().getSimpleName());

        //Add input specific options to be loaded
        for (final Option option : input.getOptionsAsIterable()) {
            AllOptions.addOption(option);
        }
        setProperty(SettingNames.INPUT, input.getClass().getSimpleName());
        /* Save this for maybe later implementation? for now trust user to specify all
        //Add algorithm required input options to be loaded
        for (final Class<? extends Input> clazz : algorithm.getRequiredInputsAsIterable()) {
            final Input input = (Input) MainProgram.getObjectFromGenericClass(clazz, Input.class);
            for (final Option addOption : input.getOptionsAsIterable()) {
                AllOptions.addOption(addOption);
            }
        }
        */

        final CommandLineParser parser = new PosixParser();
        //Load commandline options and check to make sure all required options are present
        final CommandLine line = parser.parse(AllOptions, args, false);
        for (final Object object : AllOptions.getOptions()) {
            if (object instanceof Option) {
                final Option option = Option.class.cast(object);
                if (line.hasOption(option.getOpt())) {
                    setProperty(SettingNames.valueOf(option.getLongOpt()), line.getOptionValue(option.getOpt()));
                } else {
                    if (option.isRequired()) {
                        throw new MissingArgumentException(option);
                    }
                }
            }
        }
    }

    /**
     * Get a property.
     * @param key SettingsName of property
     * @return property value of given key. returns null if the property is not found.
     */
    public static String getProperty(SettingNames key) {
        return CURRENT_PROPERTIES.getProperty(key.toString());
    }

    /**
     * Set a property.
     * @param name SettingsName of property
     * @param value value
     */
    private static void setProperty(SettingNames name, String value) {
        CURRENT_PROPERTIES.setProperty(name.toString(), value);
    }

    /**
     * Check if has property.
     * @param key SettingsName of property
     * @return true if has property, false otherwise.
     */
    public static boolean hasProperty(SettingNames key) {
        return CURRENT_PROPERTIES.containsKey(key.toString());
    }


    /*
    private static final String DEFAULT_PROPERTIES_FILE = "de/tuberlin/dima/schubotz/fse/defaultSettings";

    public static void loadConfig(String configFilePath) throws IOException {
        try (InputStream defaultConfigIS = Settings.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            DEFAULT_PROPERTIES.load(defaultConfigIS);
            try (InputStream defaultPlanConfigIS = Settings.class
                    .getResourceAsStream(DEFAULT_PROPERTIES.getProperty("planSettings"))) {
                DEFAULT_PROPERTIES.load(defaultPlanConfigIS);
            }
            CURRENT_PROPERTIES = new Properties(DEFAULT_PROPERTIES);
        } catch (final IOException ignore) {
            throw new MissingResourceException(
                    "Default properties not found. Rebuild project. This should not be called.",
                    Settings.class.getName(),
                    DEFAULT_PROPERTIES_FILE);
        }

        try (InputStream configIS = new FileInputStream(new File(configFilePath))) {
            CURRENT_PROPERTIES.load(configIS);
        }
        //Reload plan settings in case they changed
        try (InputStream planConfigIS = Settings.class
                .getResourceAsStream(CURRENT_PROPERTIES.getProperty("planSettings"))) {
            CURRENT_PROPERTIES.load(planConfigIS);
        }
    }
    */
}
