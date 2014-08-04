package de.tuberlin.dima.schubotz.common.utils;

import org.apache.commons.logging.Log;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Wraps around Stratosphere's log4j log to provide log level guards as well as level customization.
 */
public class SafeLogWrapper {
    private final Log logger;
    private final Class logClass;
    /**
     * @param logClass class to which this log belongs
     * @param rawLog log to wrap around
     */
    public SafeLogWrapper(Class logClass, Log rawLog) {
        this.logger = rawLog;
        this.logClass = logClass;
    }
    /**
     * Set level for the logger.
     * @param level log level
     */
    public void setLevel(Level level) {
        Logger.getLogger(logClass).setLevel(level);
    }
    public void fatal(String msg) {
        if (logger.isFatalEnabled()) {
            logger.fatal(msg);
        }
    }
    public void fatal(String msg, Throwable t) {
        if (logger.isFatalEnabled()) {
            logger.fatal(msg, t);
        }
    }
    public void error(String msg) {
        if (logger.isErrorEnabled()) {
            logger.error(msg);
        }
    }
    public void error(String msg, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error(msg);
        }
    }
    public void warn(String msg) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg);
        }
    }
    public void warn(String msg, Throwable t) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg, t);
        }
    }
    public void info(String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }
    public void info(String msg, Throwable t) {
        if (logger.isInfoEnabled()) {
            logger.info(msg, t);
        }
    }
    public void debug(String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }
    public void debug(String msg, Throwable t) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg, t);
        }
    }

}
