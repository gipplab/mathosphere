package de.tuberlin.dima.schubotz.fse.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Category;
import org.apache.log4j.Level;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Wraps around an Apache Commons log interface to provide log level guards as well as level customization.
 * Takes in any objects, removes all throwables and sends the last one, and then takes the remaining
 * objects and runs {@link SafeLogWrapper#buildString(Iterable)}
 * //TODO determine if custom level is necessary
 */
public class SafeLogWrapper {
    private final Log logger;

    private SafeLogWrapperLevel level = SafeLogWrapperLevel.DEBUG;
    /**
     * @param logClass class to which this log belongs
     */
    public SafeLogWrapper(Class logClass) {
        logger = LogFactory.getLog(logClass);
        //Set default level to current logger level
        level = SafeLogWrapperLevel.valueOf(((Category) logger).getLevel().toString());

    }
    /**
     * Set level for the logger. This is a hack due to commons not allowing you to set the level.
     * @param level specify this using SafeLogWrapper level fields (e.g. SafeLogWrapper.FATAL)
     */
    public void setLevel(SafeLogWrapperLevel level) {
        try {
            ((Category) logger).setLevel(Level.toLevel(level.toString()));
        } catch (final ClassCastException e) {
            warn("Unable to cast to Log4j. Did Apache Flink switch loggers?", e);
        }
        this.level = level;
    }
    public void fatal(Object... params) {
        if (logger.isFatalEnabled() && level.compareTo(SafeLogWrapperLevel.FATAL) <= 0) {
            Throwable ex = removeThrowable(params);
            logger.fatal(outputMsg(params), ex);
        }
    }
    public void error(Object... params) {
        if (logger.isFatalEnabled() && level.compareTo(SafeLogWrapperLevel.ERROR) <= 0) {
            Throwable ex = removeThrowable(params);
            logger.error(outputMsg(params), ex);
        }
    }
    public void warn(Object... params) {
        if (logger.isWarnEnabled() && level.compareTo(SafeLogWrapperLevel.WARN) <= 0) {
            Throwable ex = removeThrowable(params);
            logger.warn(outputMsg(params), ex);
        }
    }
    public void info(Object... params) {
        if (logger.isInfoEnabled() && level.compareTo(SafeLogWrapperLevel.INFO) <= 0) {
            Throwable ex = removeThrowable(params);
            logger.info(outputMsg(params), ex);
        }
    }
    public void debug(Object... params) {
        if (logger.isDebugEnabled() && level.compareTo(SafeLogWrapperLevel.DEBUG) <= 0) {
            Throwable ex = removeThrowable(params);
            logger.debug(outputMsg(params), ex);
        }
    }
    private String outputMsg(Object... params) {
        final Collection<Object> paramSet = new HashSet<>(Arrays.asList(params));
         //Remove first throwable from the set
        removeThrowable(params);
        return buildString(paramSet);
    }
    private static String buildString(Iterable<Object> params) {
        final StringBuilder builder = new StringBuilder();
        for (final Object param : params) {
            builder.append(param.toString());
        }
        return builder.toString();

    }

    /**
     * Removes first throwable from set
     * @param params
     * @return
     */
    private static Throwable removeThrowable(Object... params) {
        for (final Object param : params) {
            if (param instanceof Throwable) {
                return (Throwable) param;
            }
        }
        return null;
    }

    /**
     * To check if a level is below another level, use <code>level.compareTo(anotherLevel) < 0</code>
     */
    public enum SafeLogWrapperLevel {
        DEBUG, INFO, WARN, ERROR, FATAL, OFF
    }
}
