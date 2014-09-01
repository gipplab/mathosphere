package de.tuberlin.dima.schubotz.fse.modules.inputs.raw;

import de.tuberlin.dima.schubotz.fse.mappers.cleaners.ArxivCleaner;
import de.tuberlin.dima.schubotz.fse.mappers.cleaners.QueryCleaner;

/**
 * Created by jjl4 on 8/7/14.
 */
public class WikiRawInput extends RawInput {
    public WikiRawInput() {
        super(new QueryCleaner(), new ArxivCleaner());
    }
}
