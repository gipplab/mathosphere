package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;

/**
 * Cleaners take in raw document data and clean them.
 */
public abstract class Cleaner extends FlatMapFunction<String, RawDataTuple> {
    /**
     * @return delimiter for Stratosphere to split on.
     */
    public abstract String getDelimiter();
}
