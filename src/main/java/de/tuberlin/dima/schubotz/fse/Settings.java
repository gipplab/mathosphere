package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;


import java.io.*;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * Parses config files. Is statically initialized with default properties.
 * TODO implement settings checking for required settings
 */
public class Settings {
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    private static final String DEFAULT_PROPERTIES_FILE = "de/tuberlin/dima/schubotz/fse/defaultSettings";
    private static Properties CURRENT_PROPERTIES;
    private static final SafeLogWrapper LOG = new SafeLogWrapper(Settings.class);

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

    /**
     * Get a property.
     * @param key
     * @return property value of given key. returns null if the property is not found.
     */
    public static String getProperty(String key) {
        return CURRENT_PROPERTIES.getProperty(key);
    }

    /**
     * Set a property.
     * @param key
     * @param value
     */
    public static void setProperty(String key, String value) {
        CURRENT_PROPERTIES.setProperty(key, value);
    }

    /**
     * List of all accepted properties
     */
    public enum Properties {
        ALGORITHM,

    }
}
