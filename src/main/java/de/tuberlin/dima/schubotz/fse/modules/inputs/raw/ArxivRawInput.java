package de.tuberlin.dima.schubotz.fse.modules.inputs.raw;

import de.tuberlin.dima.schubotz.fse.mappers.cleaners.ArxivCleaner;
import de.tuberlin.dima.schubotz.fse.mappers.cleaners.QueryCleaner;

/**
 * Created by Jimmy on 8/30/2014.
 */
public class ArxivRawInput extends RawInput {
    public ArxivRawInput() {
        super(new QueryCleaner(), new ArxivCleaner());
    }
}
