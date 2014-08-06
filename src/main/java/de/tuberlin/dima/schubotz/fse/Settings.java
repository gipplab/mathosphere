package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;


import java.io.*;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * Parses config files. Is statically initialized with default properties.
 */
public class Settings {
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    private static final String DEFAULT_PROPERTIES_FILE = "de/tuberlin/dima/schubotz/fse/defaultSettings";
    private static Properties CURRENT_PROPERTIES;
    private static final SafeLogWrapper LOG = new SafeLogWrapper(Settings.class);
    static {
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

    }

    public static void loadConfig(String configFilePath) throws IOException {
        try (InputStream configIS = new FileInputStream(new File(configFilePath))) {
            CURRENT_PROPERTIES.load(configIS);
        }
    }
    public static String getProperty(String key) {
        return CURRENT_PROPERTIES.getProperty(key);
    }
}
