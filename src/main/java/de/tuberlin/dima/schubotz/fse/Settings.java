package de.tuberlin.dima.schubotz.fse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Properties;

/**
 * Parses config files.
 */
public class Settings {
    private static final Properties DEFAULT_PROPERTIES = new Properties();
    private static final String DEFAULT_PROPERTIES_FILE = "de/tuberlin/dima/schubotz/fse/defaultSettings";
    private static final Log LOG = new SafeLogWrapper(LogFactory.getLog(Settings.class));
    static {
        try (InputStream defaultConfigIS = Settings.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            DEFAULT_PROPERTIES.load(defaultConfigIS);
        } catch (IOException e) {

        }

    }

    public Settings(String configFilePath) throws IOException {
        try (InputStream configIS = new FileInputStream(new File(configFilePath))) {
            CURRENT_PROPERTIES.load(configIS);
        }
    }
}
