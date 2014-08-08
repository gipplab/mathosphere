package de.tuberlin.dima.schubotz.fse.settings;

import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;
import de.tuberlin.dima.schubotz.fse.modules.Module;
import de.tuberlin.dima.schubotz.fse.modules.algorithms.Algorithm;
import org.apache.commons.cli.*;


import java.util.Properties;

/**
 * Parses config files and command lines.
 * Is statically initialized with default properties.
 * Used to generate Properties objects
 */
public class Settings {
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
    private static final Option INPUT_OPTION = new Option(
            SettingNames.INPUT_OPTION.getLetter(), SettingNames.INPUT_OPTION.toString(),
            true, "Input class to run.");
    private static final Option DATA_FILE = new Option(
            SettingNames.DATA_FILE.getLetter(), SettingNames.DATA_FILE.toString(),
            true, "Path to data file.");
    private static final Option QUERY_FILE = new Option(
            SettingNames.QUERY_FILE.getLetter(), SettingNames.QUERY_FILE.toString(),
            true, "Path to query file.");
    private static final Option RUNTAG = new Option(
            SettingNames.RUNTAG.getLetter(), SettingNames.RUNTAG.toString(),
            true, "Runtag name.");
    private static final Option NUM_SUB_TASKS = new Option(
            SettingNames.NUM_SUB_TASKS.getLetter(), SettingNames.NUM_SUB_TASKS.toString(),
            true, "Parellelization");
    private static final Option OUTPUT_DIR = new Option(
            SettingNames.OUTPUT_DIR.getLetter(), SettingNames.OUTPUT_DIR.toString(),
            true, "Directory for output files.");
    private static final Option OUTPUT_OPTION = new Option(
            SettingNames.OUTPUT_OPTION.getLetter(), SettingNames.OUTPUT_OPTION.toString(),
            true, "Output class to run.");

    static {
        INPUT_OPTION.setRequired(true);
        INPUT_OPTION.setArgName("input-format");

        DATA_FILE.setRequired(true);
        DATA_FILE.setArgName("/path/to/data");

        QUERY_FILE.setRequired(true);
        QUERY_FILE.setArgName("/path/to/queries");

        RUNTAG.setRequired(true);
        RUNTAG.setArgName("runtag");

        NUM_SUB_TASKS.setRequired(true);
        NUM_SUB_TASKS.setArgName("num-subtasks");

        OUTPUT_OPTION.setRequired(true);
        OUTPUT_OPTION.setArgName("output-format");

        OUTPUT_DIR.setRequired(true);
        OUTPUT_DIR.setArgName("/path/to/output");

        GeneralOptions.addOption(INPUT_OPTION);
        GeneralOptions.addOption(DATA_FILE);
        GeneralOptions.addOption(QUERY_FILE);
        GeneralOptions.addOption(RUNTAG);
        GeneralOptions.addOption(NUM_SUB_TASKS);
        GeneralOptions.addOption(OUTPUT_OPTION);
        GeneralOptions.addOption(OUTPUT_DIR);

        for (final Object option : GeneralOptions.getOptions()) {
            if (option instanceof Option) {
                AllOptions.addOption((Option) option);
            }
        }
    }

    /**
     * Load options from arguments for specific algorithm.
     * @param args arguments
     * @param algorithm algorithm
     * @throws ParseException commandlineparser may fail or options may be missing
     */
    public static void loadOptions(String[] args, Algorithm algorithm) throws ParseException {
        final CommandLineParser parser = new PosixParser();
        //Add algorithm specific options
        for (final Option option : algorithm.getOptionsAsIterable()) {
            AllOptions.addOption(option);
        }
        //Add algorithm required input options
        for (final Class clazz : algorithm.getRequiredInputsAsIterable()) {
            if (Module.class.isAssignableFrom(clazz)) {
                (Module) clazz

            }
            for (final Option addOption : clazz.getOptionsAsIterable()) {
                AllOptions.addOption(addOption);
            }
        }

        //Load commandline options, check to make sure it contains all required options
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
