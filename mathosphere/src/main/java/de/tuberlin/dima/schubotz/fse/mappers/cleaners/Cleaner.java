package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.api.common.functions.FlatMapFunction;

/**
 * Cleaners take in raw document data and clean them.
 */
public abstract class Cleaner implements FlatMapFunction<String, RawDataTuple> {
    /**
     * @return delimiter for Stratosphere to split on.
     */
    public abstract String getDelimiter();
}
