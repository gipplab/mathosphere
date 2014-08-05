package de.tuberlin.dima.schubotz.common.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Wraps around Stratosphere's log4j log to provide log level guards as well as level customization.
 */
public class SafeLogWrapper implements Serializable {
    private final Logger logger;
    private final Class logClass;
    /**
     * @param logClass class to which this log belongs
     */
    public SafeLogWrapper(Class logClass) {
        logger = Logger.getLogger(logClass);
        this.logClass = logClass;
    }
    /**
     * Set level for the logger.
     * @param level log level
     */
    public void setLevel(Level level) {
        Logger.getLogger(logClass).setLevel(level);
    }
    public void fatal(Object... params) {
        HashSet<Object> paramSet = new HashSet<>(Arrays.asList(params));
        //Remove any throwables from the set
        Throwable ex = removeThrowable(paramSet);
        if (logger.isEnabledFor(Level.FATAL)) {
            logger.fatal(msg);
        }
    }
    public void error(Object... params) {
        if (logger.isEnabledFor(Level.ERROR)) {
            logger.error(msg);
        }
    }
    public void warn(Object... params) {
        if (logger.isEnabledFor(Level.WARN)) {
            logger.warn(msg);
        }
    }
    public void info(Object... params) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }
    public void debug(Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }
    public String buildString(Object[] params) {

    }
    public Throwable removeThrowable(HashSet<Object> params) {
        Throwable out;
        for (final Object param : params) {
            if (param instanceof Throwable) {


            }
        }
    }


}
