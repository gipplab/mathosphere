package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;


import java.io.*;
import java.util.Properties;

/**
 * Parses config files.
 */
public class Settings {
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    private static final String DEFAULT_PROPERTIES_FILE = "de/tuberlin/dima/schubotz/fse/defaultSettings";
    private static Properties CURRENT_PROPERTIES;
    private static final SafeLogWrapper LOG = new SafeLogWrapper(Settings.class);
    static {
        try (InputStream defaultConfigIS = Settings.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            DEFAULT_PROPERTIES.load(defaultConfigIS);
            CURRENT_PROPERTIES = new Properties(DEFAULT_PROPERTIES);
        } catch (final IOException e) {
            throw new RuntimeException("Default properties not found. Rebuild project. This should not be called.", e);
        }

    }

    public Settings(String configFilePath) throws IOException {
        try (InputStream configIS = new FileInputStream(new File(configFilePath))) {
            CURRENT_PROPERTIES.load(configIS);
        }
    }
}
