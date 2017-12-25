package com.formulasearchengine.mathosphere.pomlp.pom;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * @author Andre Greiner-Petter
 */
public class LoggingConfig {
    private static final Logger LOG = LogManager.getLogger(LoggingConfig.class.getName());

    public static final String MAVEN_LOG_LEVEL = "maven.loglevel";

    public static void configLog(){
        try {
            String mavenLog = System.getProperty( MAVEN_LOG_LEVEL );
            if ( mavenLog != null ){
                Configurator.setAllLevels(
                        "com.formulasearchengine.mathosphere.pomlp.convertor.MathParser",
                        Level.getLevel(mavenLog)
                );
                Configurator.setRootLevel(Level.getLevel(mavenLog));
            }
        } catch ( Exception e ){
            LOG.trace("No log level specified by maven. DebugMSG: " + e.getMessage() );
        }
    }

}
